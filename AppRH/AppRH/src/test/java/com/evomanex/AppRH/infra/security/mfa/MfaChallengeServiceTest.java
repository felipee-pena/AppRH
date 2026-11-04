package com.evomanex.AppRH.infra.security.mfa;

import com.evomanex.AppRH.domain.mfa.MfaChallenge;
import com.evomanex.AppRH.domain.mfa.UserMfa;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.MfaChallengeRepository;
import com.evomanex.AppRH.repository.UserMfaRepository;
import com.evomanex.AppRH.service.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaChallengeServiceTest {

    @Mock
    private MfaChallengeRepository challengeRepository;

    @Mock
    private UserMfaRepository userMfaRepository;

    @Mock
    private TotpService totpService;

    @Mock
    private SecretEncryptionService encryptionService;

    @Mock
    private HashService hashService;

    @Mock
    private User user;

    @Mock
    private UserMfa userMfa;

    private MfaChallengeService service;

    @BeforeEach
    void setUp() {
        service =
                new MfaChallengeService(
                        challengeRepository,
                        userMfaRepository,
                        totpService,
                        encryptionService,
                        hashService
                );
    }

    @Test
    void shouldCreateAndPersistChallenge() {
        when(
                hashService.sha256(
                        org.mockito.ArgumentMatchers
                                .anyString()
                )
        ).thenReturn(
                "challenge-hash"
        );

        String rawChallenge =
                service.createChallenge(
                        user
                );

        assertThat(
                rawChallenge
        ).isNotBlank();

        verify(
                hashService
        ).sha256(
                rawChallenge
        );

        ArgumentCaptor<MfaChallenge> captor =
                ArgumentCaptor.forClass(
                        MfaChallenge.class
                );

        verify(
                challengeRepository
        ).save(
                captor.capture()
        );

        MfaChallenge savedChallenge =
                captor.getValue();

        assertThat(
                savedChallenge.getUser()
        ).isSameAs(
                user
        );

        String challengeHash =
                (String) ReflectionTestUtils.getField(
                        savedChallenge,
                        "challengeHash"
                );

        OffsetDateTime expiresAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        savedChallenge,
                        "expiresAt"
                );

        assertThat(
                challengeHash
        ).isEqualTo(
                "challenge-hash"
        );

        assertThat(
                expiresAt
        ).isAfter(
                OffsetDateTime.now()
                        .plusMinutes(4)
        );

        assertThat(
                expiresAt
        ).isBefore(
                OffsetDateTime.now()
                        .plusMinutes(6)
        );
    }

    @Test
    void shouldVerifyValidTotp() {
        String rawChallenge =
                "raw-challenge";

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        OffsetDateTime.now()
                                .plusMinutes(5)
                );

        when(
                hashService.sha256(
                        rawChallenge
                )
        ).thenReturn(
                "challenge-hash"
        );

        when(
                challengeRepository
                        .findByChallengeHashAndConsumedAtIsNull(
                                "challenge-hash"
                        )
        ).thenReturn(
                Optional.of(
                        challenge
                )
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
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
                "raw-secret"
        );

        when(
                totpService.verify(
                        "raw-secret",
                        "123456"
                )
        ).thenReturn(
                true
        );

        User result =
                service.verifyTotp(
                        rawChallenge,
                        "123456"
                );

        assertThat(
                result
        ).isSameAs(
                user
        );

        OffsetDateTime consumedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        challenge,
                        "consumedAt"
                );

        assertThat(
                consumedAt
        ).isNotNull();

        verify(
                userMfa
        ).registerUse();
    }

    @Test
    void shouldRejectUnknownChallenge() {
        when(
                hashService.sha256(
                        "unknown-challenge"
                )
        ).thenReturn(
                "unknown-hash"
        );

        when(
                challengeRepository
                        .findByChallengeHashAndConsumedAtIsNull(
                                "unknown-hash"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.verifyTotp(
                                "unknown-challenge",
                                "123456"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Desafio MFA inválido."
                );
    }

    @Test
    void shouldRejectExpiredChallenge() {
        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        OffsetDateTime.now()
                                .minusSeconds(1)
                );

        when(
                hashService.sha256(
                        "expired-challenge"
                )
        ).thenReturn(
                "challenge-hash"
        );

        when(
                challengeRepository
                        .findByChallengeHashAndConsumedAtIsNull(
                                "challenge-hash"
                        )
        ).thenReturn(
                Optional.of(
                        challenge
                )
        );

        assertThatThrownBy(
                () ->
                        service.verifyTotp(
                                "expired-challenge",
                                "123456"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Desafio MFA expirado, consumido ou bloqueado."
                );
    }

    @Test
    void shouldRejectWhenMfaIsNotEnabled() {
        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        OffsetDateTime.now()
                                .plusMinutes(5)
                );

        when(
                hashService.sha256(
                        "raw-challenge"
                )
        ).thenReturn(
                "challenge-hash"
        );

        when(
                challengeRepository
                        .findByChallengeHashAndConsumedAtIsNull(
                                "challenge-hash"
                        )
        ).thenReturn(
                Optional.of(
                        challenge
                )
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                userMfaRepository
                        .findByUser_IdAndEnabledTrue(
                                "user-uuid-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.verifyTotp(
                                "raw-challenge",
                                "123456"
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "MFA não está habilitado."
                );
    }

    @Test
    void shouldRegisterFailureForInvalidTotp() {
        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        OffsetDateTime.now()
                                .plusMinutes(5)
                );

        when(
                hashService.sha256(
                        "raw-challenge"
                )
        ).thenReturn(
                "challenge-hash"
        );

        when(
                challengeRepository
                        .findByChallengeHashAndConsumedAtIsNull(
                                "challenge-hash"
                        )
        ).thenReturn(
                Optional.of(
                        challenge
                )
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
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
                "raw-secret"
        );

        when(
                totpService.verify(
                        "raw-secret",
                        "999999"
                )
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.verifyTotp(
                                "raw-challenge",
                                "999999"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Código MFA inválido."
                );

        Integer failedAttempts =
                (Integer) ReflectionTestUtils.getField(
                        challenge,
                        "failedAttempts"
                );

        assertThat(
                failedAttempts
        ).isEqualTo(
                1
        );

        verify(
                userMfa,
                never()
        ).registerUse();
    }
}