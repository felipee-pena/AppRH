package com.evomanex.AppRH.infra.security.mfa;

import com.evomanex.AppRH.domain.mfa.MfaChallenge;
import com.evomanex.AppRH.domain.mfa.UserMfa;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.MfaChallengeRepository;
import com.evomanex.AppRH.repository.UserMfaRepository;
import com.evomanex.AppRH.service.HashService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
public class MfaChallengeService {

    private final MfaChallengeRepository
            challengeRepository;

    private final UserMfaRepository
            userMfaRepository;

    private final TotpService
            totpService;

    private final SecretEncryptionService
            encryptionService;

    private final HashService
            hashService;

    private final SecureRandom
            secureRandom =
            new SecureRandom();

    public MfaChallengeService(
            MfaChallengeRepository challengeRepository,
            UserMfaRepository userMfaRepository,
            TotpService totpService,
            SecretEncryptionService encryptionService,
            HashService hashService
    ) {
        this.challengeRepository =
                challengeRepository;

        this.userMfaRepository =
                userMfaRepository;

        this.totpService =
                totpService;

        this.encryptionService =
                encryptionService;

        this.hashService =
                hashService;
    }

    @Transactional
    public String createChallenge(
            User user
    ) {
        byte[] bytes =
                new byte[32];

        secureRandom.nextBytes(
                bytes
        );

        String rawChallenge =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                bytes
                        );

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        hashService.sha256(
                                rawChallenge
                        ),
                        OffsetDateTime
                                .now()
                                .plusMinutes(5)
                );

        challengeRepository.save(
                challenge
        );

        return rawChallenge;
    }

    @Transactional
    public User verifyTotp(
            String rawChallenge,
            String code
    ) {
        String challengeHash =
                hashService.sha256(
                        rawChallenge
                );

        MfaChallenge challenge =
                challengeRepository
                        .findByChallengeHashAndConsumedAtIsNull(
                                challengeHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Desafio MFA inválido."
                                        )
                        );

        if (
                !challenge.isAvailable(
                        OffsetDateTime.now()
                )
        ) {
            throw new IllegalArgumentException(
                    "Desafio MFA expirado, consumido "
                            + "ou bloqueado."
            );
        }

        User user =
                challenge.getUser();

        UserMfa userMfa =
                userMfaRepository
                        .findByUser_IdAndEnabledTrue(
                                user.getId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "MFA não está habilitado."
                                        )
                        );

        String secret =
                encryptionService.decrypt(
                        userMfa.getSecretEncrypted()
                );

        boolean valid =
                totpService.verify(
                        secret,
                        code
                );

        if (!valid) {
            challenge.registerFailure();

            throw new IllegalArgumentException(
                    "Código MFA inválido."
            );
        }

        challenge.consume();

        userMfa.registerUse();

        return user;
    }
}