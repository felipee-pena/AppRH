package com.evomanex.AppRH.infra.security.mfa;

import com.evomanex.AppRH.domain.mfa.UserMfa;
import com.evomanex.AppRH.domain.mfa.UserMfaRecoveryCode;
import com.evomanex.AppRH.domain.user.MfaSetupResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.UserMfaRecoveryCodeRepository;
import com.evomanex.AppRH.repository.UserMfaRepository;
import com.evomanex.AppRH.service.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private UserMfaRepository userMfaRepository;

    @Mock
    private UserMfaRecoveryCodeRepository
            recoveryCodeRepository;

    @Mock
    private TotpService totpService;

    @Mock
    private SecretEncryptionService encryptionService;

    @Mock
    private HashService hashService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private User user;

    @Mock
    private UserMfa userMfa;

    @Mock
    private UserMfaRecoveryCode recoveryCode;

    private MfaService service;

    @BeforeEach
    void setUp() {
        service =
                new MfaService(
                        userMfaRepository,
                        recoveryCodeRepository,
                        totpService,
                        encryptionService,
                        hashService,
                        passwordEncoder
                );
    }

    @Test
    void shouldBeginMfaSetup() {
        prepareActiveUser();

        when(
                user.getLogin()
        ).thenReturn(
                "usuario@example.com"
        );

        when(
                userMfaRepository
                        .findByUser_IdAndMethod(
                                "user-uuid-123",
                                MfaMethod.TOTP
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                totpService.generateSecret()
        ).thenReturn(
                "BASE32SECRET"
        );

        when(
                encryptionService.encrypt(
                        "BASE32SECRET"
                )
        ).thenReturn(
                "encrypted-secret"
        );

        when(
                hashService.sha256(
                        org.mockito.ArgumentMatchers
                                .anyString()
                )
        ).thenReturn(
                "recovery-hash"
        );

        when(
                totpService.generateOtpAuthUri(
                        "AppRH",
                        "usuario@example.com",
                        "BASE32SECRET"
                )
        ).thenReturn(
                "otpauth://totp/AppRH"
        );

        MfaSetupResponseDTO response =
                service.beginSetup(
                        user
                );

        assertThat(
                response.secret()
        ).isEqualTo(
                "BASE32SECRET"
        );

        assertThat(
                response.otpAuthUri()
        ).isEqualTo(
                "otpauth://totp/AppRH"
        );

        assertThat(
                response.recoveryCodes()
        ).hasSize(
                8
        );

        verify(
                recoveryCodeRepository
        ).deleteAllByUser_Id(
                "user-uuid-123"
        );

        verify(
                recoveryCodeRepository
        ).flush();

        ArgumentCaptor<UserMfa> userMfaCaptor =
                ArgumentCaptor.forClass(
                        UserMfa.class
                );

        verify(
                userMfaRepository
        ).save(
                userMfaCaptor.capture()
        );

        assertThat(
                userMfaCaptor
                        .getValue()
                        .getUser()
        ).isSameAs(
                user
        );

        assertThat(
                userMfaCaptor
                        .getValue()
                        .getSecretEncrypted()
        ).isEqualTo(
                "encrypted-secret"
        );

        ArgumentCaptor<List<UserMfaRecoveryCode>> codesCaptor =
                ArgumentCaptor.forClass(
                        List.class
                );

        verify(
                recoveryCodeRepository
        ).saveAll(
                codesCaptor.capture()
        );

        assertThat(
                codesCaptor.getValue()
        ).hasSize(
                8
        );

        verify(
                hashService,
                times(8)
        ).sha256(
                org.mockito.ArgumentMatchers
                        .anyString()
        );
    }

    @Test
    void shouldRejectSetupWhenMfaIsAlreadyEnabled() {
        prepareActiveUser();

        when(
                userMfaRepository
                        .findByUser_IdAndMethod(
                                "user-uuid-123",
                                MfaMethod.TOTP
                        )
        ).thenReturn(
                Optional.of(
                        userMfa
                )
        );

        when(
                userMfa.isEnabled()
        ).thenReturn(
                true
        );

        assertThatThrownBy(
                () ->
                        service.beginSetup(
                                user
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "A autenticação em dois fatores já está habilitada."
                );

        verify(
                totpService,
                never()
        ).generateSecret();
    }

    @Test
    void shouldReplacePendingConfiguration() {
        prepareActiveUser();

        when(
                user.getLogin()
        ).thenReturn(
                "usuario@example.com"
        );

        when(
                userMfaRepository
                        .findByUser_IdAndMethod(
                                "user-uuid-123",
                                MfaMethod.TOTP
                        )
        ).thenReturn(
                Optional.of(
                        userMfa
                )
        );

        when(
                userMfa.isEnabled()
        ).thenReturn(
                false
        );

        when(
                totpService.generateSecret()
        ).thenReturn(
                "BASE32SECRET"
        );

        when(
                encryptionService.encrypt(
                        "BASE32SECRET"
                )
        ).thenReturn(
                "encrypted-secret"
        );

        when(
                hashService.sha256(
                        org.mockito.ArgumentMatchers
                                .anyString()
                )
        ).thenReturn(
                "recovery-hash"
        );

        when(
                totpService.generateOtpAuthUri(
                        "AppRH",
                        "usuario@example.com",
                        "BASE32SECRET"
                )
        ).thenReturn(
                "otpauth://totp/AppRH"
        );

        service.beginSetup(
                user
        );

        verify(
                userMfaRepository
        ).delete(
                userMfa
        );

        verify(
                userMfaRepository
        ).flush();
    }

    @Test
    void shouldConfirmMfaSetup() {
        prepareActiveUser();

        when(
                userMfaRepository
                        .findByUser_IdAndMethod(
                                "user-uuid-123",
                                MfaMethod.TOTP
                        )
        ).thenReturn(
                Optional.of(
                        userMfa
                )
        );

        when(
                userMfa.isEnabled()
        ).thenReturn(
                false
        );

        when(
                userMfa.getSecretEncrypted()
        ).thenReturn(
                "encrypted-secret"
        );

        when(
                encryptionService.decrypt(
                        "encrypted-secret"
                )
        ).thenReturn(
                "BASE32SECRET"
        );

        when(
                totpService.verify(
                        "BASE32SECRET",
                        "123456"
                )
        ).thenReturn(
                true
        );

        service.confirmSetup(
                user,
                "123456"
        );

        verify(
                userMfa
        ).confirm();

        verify(
                userMfaRepository
        ).save(
                userMfa
        );
    }

    @Test
    void shouldRejectInvalidTotpDuringConfirmation() {
        prepareActiveUser();

        when(
                userMfaRepository
                        .findByUser_IdAndMethod(
                                "user-uuid-123",
                                MfaMethod.TOTP
                        )
        ).thenReturn(
                Optional.of(
                        userMfa
                )
        );

        when(
                userMfa.isEnabled()
        ).thenReturn(
                false
        );

        when(
                userMfa.getSecretEncrypted()
        ).thenReturn(
                "encrypted-secret"
        );

        when(
                encryptionService.decrypt(
                        "encrypted-secret"
                )
        ).thenReturn(
                "BASE32SECRET"
        );

        when(
                totpService.verify(
                        "BASE32SECRET",
                        "999999"
                )
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.confirmSetup(
                                user,
                                "999999"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Código MFA inválido."
                );

        verify(
                userMfa,
                never()
        ).confirm();
    }

    @Test
    void shouldDisableMfa() {
        prepareActiveUser();

        when(
                passwordEncoder.matches(
                        "current-password",
                        "encoded-password"
                )
        ).thenReturn(
                true
        );

        when(
                user.getPassword()
        ).thenReturn(
                "encoded-password"
        );

        when(
                userMfaRepository
                        .findByUser_IdAndEnabledTrue(
                                "user-uuid-123"
                        )
        ).thenReturn(
                Optional.of(
                        userMfa
                )
        );

        when(
                userMfa.getSecretEncrypted()
        ).thenReturn(
                "encrypted-secret"
        );

        when(
                encryptionService.decrypt(
                        "encrypted-secret"
                )
        ).thenReturn(
                "BASE32SECRET"
        );

        when(
                totpService.verify(
                        "BASE32SECRET",
                        "123456"
                )
        ).thenReturn(
                true
        );

        service.disable(
                user,
                "current-password",
                "123456"
        );

        verify(
                userMfa
        ).disable();

        verify(
                userMfaRepository
        ).save(
                userMfa
        );

        verify(
                recoveryCodeRepository
        ).deleteAllByUser_Id(
                "user-uuid-123"
        );

        verify(
                recoveryCodeRepository
        ).flush();
    }

    @Test
    void shouldRejectInvalidPasswordWhenDisablingMfa() {
        prepareActiveUser();

        when(
                user.getPassword()
        ).thenReturn(
                "encoded-password"
        );

        when(
                passwordEncoder.matches(
                        "wrong-password",
                        "encoded-password"
                )
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.disable(
                                user,
                                "wrong-password",
                                "123456"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Senha atual inválida."
                );

        verify(
                userMfaRepository,
                never()
        ).findByUser_IdAndEnabledTrue(
                org.mockito.ArgumentMatchers
                        .anyString()
        );
    }

    @Test
    void shouldRegenerateRecoveryCodes() {
        prepareActiveUser();

        when(
                userMfaRepository
                        .existsByUser_IdAndEnabledTrue(
                                "user-uuid-123"
                        )
        ).thenReturn(
                true
        );

        when(
                hashService.sha256(
                        org.mockito.ArgumentMatchers
                                .anyString()
                )
        ).thenReturn(
                "recovery-hash"
        );

        List<String> codes =
                service.regenerateRecoveryCodes(
                        user
                );

        assertThat(
                codes
        ).hasSize(
                8
        );

        verify(
                recoveryCodeRepository
        ).deleteAllByUser_Id(
                "user-uuid-123"
        );

        verify(
                recoveryCodeRepository
        ).flush();

        verify(
                recoveryCodeRepository
        ).saveAll(
                org.mockito.ArgumentMatchers
                        .anyList()
        );
    }

    @Test
    void shouldRejectRecoveryCodeRegenerationWithoutMfa() {
        prepareActiveUser();

        when(
                userMfaRepository
                        .existsByUser_IdAndEnabledTrue(
                                "user-uuid-123"
                        )
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.regenerateRecoveryCodes(
                                user
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "A autenticação em dois fatores não está habilitada."
                );
    }

    @Test
    void shouldConsumeRecoveryCode() {
        prepareActiveUser();

        when(
                hashService.sha256(
                        "RECOVERYCODE"
                )
        ).thenReturn(
                "recovery-hash"
        );

        when(
                recoveryCodeRepository
                        .findByUser_IdAndCodeHashAndUsedAtIsNull(
                                "user-uuid-123",
                                "recovery-hash"
                        )
        ).thenReturn(
                Optional.of(
                        recoveryCode
                )
        );

        boolean result =
                service.consumeRecoveryCode(
                        user,
                        " recovery-code "
                );

        assertThat(
                result
        ).isTrue();

        verify(
                recoveryCode
        ).consume();

        verify(
                recoveryCodeRepository
        ).save(
                recoveryCode
        );
    }

    @Test
    void shouldRejectUnknownRecoveryCode() {
        prepareActiveUser();

        when(
                hashService.sha256(
                        "INVALIDCODE"
                )
        ).thenReturn(
                "invalid-hash"
        );

        when(
                recoveryCodeRepository
                        .findByUser_IdAndCodeHashAndUsedAtIsNull(
                                "user-uuid-123",
                                "invalid-hash"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.consumeRecoveryCode(
                                user,
                                "invalid-code"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Código de recuperação inválido ou já utilizado."
                );
    }

    @Test
    void shouldReturnMfaStatus() {
        prepareActiveUser();

        when(
                userMfaRepository
                        .existsByUser_IdAndEnabledTrue(
                                "user-uuid-123"
                        )
        ).thenReturn(
                true
        );

        boolean enabled =
                service.isMfaEnabled(
                        user
                );

        assertThat(
                enabled
        ).isTrue();
    }

    @Test
    void shouldRejectInactiveUser() {
        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                user.isEnabled()
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.isMfaEnabled(
                                user
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "A conta AppRH não está ativa."
                );
    }

    private void prepareActiveUser() {
        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );
    }
}