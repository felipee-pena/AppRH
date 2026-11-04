package com.evomanex.AppRH.infra.security.mfa;

import com.evomanex.AppRH.domain.mfa.UserMfa;
import com.evomanex.AppRH.domain.mfa.UserMfaRecoveryCode;
import com.evomanex.AppRH.domain.user.MfaSetupResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.UserMfaRecoveryCodeRepository;
import com.evomanex.AppRH.repository.UserMfaRepository;
import com.evomanex.AppRH.service.HashService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class MfaService {

    private static final int RECOVERY_CODE_COUNT =
            8;

    private static final int RECOVERY_CODE_BYTE_LENGTH =
            9;

    private final UserMfaRepository
            userMfaRepository;

    private final UserMfaRecoveryCodeRepository
            recoveryCodeRepository;

    private final TotpService
            totpService;

    private final SecretEncryptionService
            encryptionService;

    private final HashService
            hashService;

    private final PasswordEncoder
            passwordEncoder;

    private final SecureRandom
            secureRandom;

    public MfaService(
            UserMfaRepository userMfaRepository,
            UserMfaRecoveryCodeRepository recoveryCodeRepository,
            TotpService totpService,
            SecretEncryptionService encryptionService,
            HashService hashService,
            PasswordEncoder passwordEncoder
    ) {
        this.userMfaRepository =
                userMfaRepository;

        this.recoveryCodeRepository =
                recoveryCodeRepository;

        this.totpService =
                totpService;

        this.encryptionService =
                encryptionService;

        this.hashService =
                hashService;

        this.passwordEncoder =
                passwordEncoder;

        this.secureRandom =
                new SecureRandom();
    }

    @Transactional
    public MfaSetupResponseDTO beginSetup(
            User user
    ) {
        validateUser(
                user
        );

        UserMfa existingConfiguration =
                userMfaRepository
                        .findByUser_IdAndMethod(
                                user.getId(),
                                MfaMethod.TOTP
                        )
                        .orElse(
                                null
                        );

        if (
                existingConfiguration != null
                        && existingConfiguration.isEnabled()
        ) {
            throw new IllegalStateException(
                    "A autenticação em dois fatores "
                            + "já está habilitada."
            );
        }

        if (existingConfiguration != null) {
            userMfaRepository.delete(
                    existingConfiguration
            );

            userMfaRepository.flush();
        }

        recoveryCodeRepository
                .deleteAllByUser_Id(
                        user.getId()
                );

        recoveryCodeRepository.flush();

        String rawSecret =
                totpService.generateSecret();

        String encryptedSecret =
                encryptionService.encrypt(
                        rawSecret
                );

        UserMfa userMfa =
                new UserMfa(
                        user,
                        MfaMethod.TOTP,
                        encryptedSecret
                );

        userMfaRepository.save(
                userMfa
        );

        List<String> recoveryCodes =
                createRecoveryCodes(
                        user
                );

        String otpAuthUri =
                totpService.generateOtpAuthUri(
                        "AppRH",
                        user.getLogin(),
                        rawSecret
                );

        return new MfaSetupResponseDTO(
                rawSecret,
                otpAuthUri,
                recoveryCodes
        );
    }

    @Transactional
    public void confirmSetup(
            User user,
            String code
    ) {
        validateUser(
                user
        );

        validateTotpCode(
                code
        );

        UserMfa userMfa =
                userMfaRepository
                        .findByUser_IdAndMethod(
                                user.getId(),
                                MfaMethod.TOTP
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "A configuração MFA "
                                                        + "não foi iniciada."
                                        )
                        );

        if (userMfa.isEnabled()) {
            throw new IllegalStateException(
                    "A autenticação em dois fatores "
                            + "já está habilitada."
            );
        }

        String decryptedSecret =
                encryptionService.decrypt(
                        userMfa.getSecretEncrypted()
                );

        boolean validCode =
                totpService.verify(
                        decryptedSecret,
                        code
                );

        if (!validCode) {
            throw new IllegalArgumentException(
                    "Código MFA inválido."
            );
        }

        userMfa.confirm();

        userMfaRepository.save(
                userMfa
        );
    }

    @Transactional
    public void disable(
            User user,
            String password,
            String code
    ) {
        validateUser(
                user
        );

        validatePassword(
                password
        );

        validateTotpCode(
                code
        );

        boolean validPassword =
                passwordEncoder.matches(
                        password,
                        user.getPassword()
                );

        if (!validPassword) {
            throw new IllegalArgumentException(
                    "Senha atual inválida."
            );
        }

        UserMfa userMfa =
                userMfaRepository
                        .findByUser_IdAndEnabledTrue(
                                user.getId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "A autenticação em dois "
                                                        + "fatores não está habilitada."
                                        )
                        );

        String decryptedSecret =
                encryptionService.decrypt(
                        userMfa.getSecretEncrypted()
                );

        boolean validCode =
                totpService.verify(
                        decryptedSecret,
                        code
                );

        if (!validCode) {
            throw new IllegalArgumentException(
                    "Código MFA inválido."
            );
        }

        userMfa.disable();

        userMfaRepository.save(
                userMfa
        );

        recoveryCodeRepository
                .deleteAllByUser_Id(
                        user.getId()
                );

        recoveryCodeRepository.flush();
    }

    @Transactional
    public List<String> regenerateRecoveryCodes(
            User user
    ) {
        validateUser(
                user
        );

        boolean mfaEnabled =
                userMfaRepository
                        .existsByUser_IdAndEnabledTrue(
                                user.getId()
                        );

        if (!mfaEnabled) {
            throw new IllegalStateException(
                    "A autenticação em dois fatores "
                            + "não está habilitada."
            );
        }

        recoveryCodeRepository
                .deleteAllByUser_Id(
                        user.getId()
                );

        recoveryCodeRepository.flush();

        return createRecoveryCodes(
                user
        );
    }

    @Transactional
    public boolean consumeRecoveryCode(
            User user,
            String rawRecoveryCode
    ) {
        validateUser(
                user
        );

        if (
                rawRecoveryCode == null
                        || rawRecoveryCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "O código de recuperação "
                            + "é obrigatório."
            );
        }

        String normalizedCode =
                normalizeRecoveryCode(
                        rawRecoveryCode
                );

        String codeHash =
                hashService.sha256(
                        normalizedCode
                );

        UserMfaRecoveryCode recoveryCode =
                recoveryCodeRepository
                        .findByUser_IdAndCodeHashAndUsedAtIsNull(
                                user.getId(),
                                codeHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Código de recuperação "
                                                        + "inválido ou já utilizado."
                                        )
                        );

        recoveryCode.consume();

        recoveryCodeRepository.save(
                recoveryCode
        );

        return true;
    }

    public boolean isMfaEnabled(
            User user
    ) {
        validateUser(
                user
        );

        return userMfaRepository
                .existsByUser_IdAndEnabledTrue(
                        user.getId()
                );
    }

    private List<String> createRecoveryCodes(
            User user
    ) {
        List<String> rawCodes =
                new ArrayList<>();

        List<UserMfaRecoveryCode> entities =
                new ArrayList<>();

        for (
                int index = 0;
                index < RECOVERY_CODE_COUNT;
                index++
        ) {
            String rawCode =
                    generateRecoveryCode();

            String codeHash =
                    hashService.sha256(
                            rawCode
                    );

            UserMfaRecoveryCode entity =
                    new UserMfaRecoveryCode(
                            user,
                            codeHash
                    );

            rawCodes.add(
                    rawCode
            );

            entities.add(
                    entity
            );
        }

        recoveryCodeRepository.saveAll(
                entities
        );

        return List.copyOf(
                rawCodes
        );
    }

    private String generateRecoveryCode() {
        byte[] bytes =
                new byte[
                        RECOVERY_CODE_BYTE_LENGTH
                        ];

        secureRandom.nextBytes(
                bytes
        );

        String generatedCode =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                bytes
                        )
                        .toUpperCase(
                                Locale.ROOT
                        );

        return normalizeRecoveryCode(
                generatedCode
        );
    }

    private String normalizeRecoveryCode(
            String recoveryCode
    ) {
        return recoveryCode
                .trim()
                .replace(
                        "-",
                        ""
                )
                .replace(
                        " ",
                        ""
                )
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private void validateTotpCode(
            String code
    ) {
        if (
                code == null
                        || !code.matches(
                        "\\d{6}"
                )
        ) {
            throw new IllegalArgumentException(
                    "O código MFA deve possuir "
                            + "6 dígitos."
            );
        }
    }

    private void validatePassword(
            String password
    ) {
        if (
                password == null
                        || password.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "A senha atual é obrigatória."
            );
        }
    }

    private void validateUser(
            User user
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "O usuário é obrigatório."
            );
        }

        if (
                user.getId() == null
                        || user.getId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "O usuário não possui "
                            + "identificador válido."
            );
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException(
                    "A conta AppRH não está ativa."
            );
        }
    }
}