package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.oauth.OAuthLoginCode;
import com.evomanex.AppRH.repository.OAuthLoginCodeRepository;
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
class OAuthLoginCodeServiceTest {

    @Mock
    private OAuthLoginCodeRepository repository;

    @Mock
    private HashService hashService;

    @Mock
    private User user;

    private OAuthLoginCodeService service;

    @BeforeEach
    void setUp() {
        service =
                new OAuthLoginCodeService(
                        repository,
                        hashService
                );
    }

    @Test
    void shouldCreateAndPersistOAuthCode() {
        when(
                hashService.sha256(
                        org.mockito.ArgumentMatchers
                                .anyString()
                )
        ).thenReturn(
                "generated-code-hash"
        );

        String rawCode =
                service.createCode(
                        user
                );

        assertThat(
                rawCode
        ).isNotBlank();

        assertThat(
                rawCode
        ).doesNotContain(
                "="
        );

        verify(
                hashService
        ).sha256(
                rawCode
        );

        ArgumentCaptor<OAuthLoginCode> captor =
                ArgumentCaptor.forClass(
                        OAuthLoginCode.class
                );

        verify(
                repository
        ).save(
                captor.capture()
        );

        OAuthLoginCode savedCode =
                captor.getValue();

        String savedHash =
                (String) ReflectionTestUtils.getField(
                        savedCode,
                        "codeHash"
                );

        OffsetDateTime expiresAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        savedCode,
                        "expiresAt"
                );

        assertThat(
                savedHash
        ).isEqualTo(
                "generated-code-hash"
        );

        assertThat(
                savedCode.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                expiresAt
        ).isAfter(
                OffsetDateTime.now()
                        .plusMinutes(1)
        );

        assertThat(
                expiresAt
        ).isBefore(
                OffsetDateTime.now()
                        .plusMinutes(3)
        );
    }

    @Test
    void shouldGenerateDifferentRawCodes() {
        when(
                hashService.sha256(
                        org.mockito.ArgumentMatchers
                                .anyString()
                )
        ).thenReturn(
                "hash-value"
        );

        String firstCode =
                service.createCode(
                        user
                );

        String secondCode =
                service.createCode(
                        user
                );

        assertThat(
                firstCode
        ).isNotEqualTo(
                secondCode
        );
    }

    @Test
    void shouldRejectNullUserWhenCreatingCode() {
        assertThatThrownBy(
                () ->
                        service.createCode(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O usuário é obrigatório."
                );

        verify(
                repository,
                never()
        ).save(
                org.mockito.ArgumentMatchers
                        .any(
                                OAuthLoginCode.class
                        )
        );
    }

    @Test
    void shouldConsumeValidOAuthCode() {
        String rawCode =
                "raw-oauth-code";

        String codeHash =
                "oauth-code-hash";

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        codeHash,
                        OffsetDateTime.now()
                                .plusMinutes(1)
                );

        when(
                hashService.sha256(
                        rawCode
                )
        ).thenReturn(
                codeHash
        );

        when(
                repository
                        .findByCodeHashAndConsumedAtIsNull(
                                codeHash
                        )
        ).thenReturn(
                Optional.of(
                        loginCode
                )
        );

        User result =
                service.consumeCode(
                        rawCode
                );

        assertThat(
                result
        ).isSameAs(
                user
        );

        OffsetDateTime consumedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        loginCode,
                        "consumedAt"
                );

        assertThat(
                consumedAt
        ).isNotNull();

        verify(
                repository
        ).save(
                loginCode
        );
    }

    @Test
    void shouldRejectNullRawCode() {
        assertThatThrownBy(
                () ->
                        service.consumeCode(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O código OAuth é obrigatório."
                );

        verify(
                hashService,
                never()
        ).sha256(
                org.mockito.ArgumentMatchers
                        .anyString()
        );
    }

    @Test
    void shouldRejectBlankRawCode() {
        assertThatThrownBy(
                () ->
                        service.consumeCode(
                                " "
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O código OAuth é obrigatório."
                );
    }

    @Test
    void shouldRejectUnknownOAuthCode() {
        when(
                hashService.sha256(
                        "unknown-code"
                )
        ).thenReturn(
                "unknown-hash"
        );

        when(
                repository
                        .findByCodeHashAndConsumedAtIsNull(
                                "unknown-hash"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        service.consumeCode(
                                "unknown-code"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Código OAuth inválido."
                );
    }

    @Test
    void shouldRejectExpiredOAuthCode() {
        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "expired-hash",
                        OffsetDateTime.now()
                                .minusSeconds(1)
                );

        when(
                hashService.sha256(
                        "expired-code"
                )
        ).thenReturn(
                "expired-hash"
        );

        when(
                repository
                        .findByCodeHashAndConsumedAtIsNull(
                                "expired-hash"
                        )
        ).thenReturn(
                Optional.of(
                        loginCode
                )
        );

        assertThatThrownBy(
                () ->
                        service.consumeCode(
                                "expired-code"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Código OAuth expirado ou já consumido."
                );

        verify(
                repository,
                never()
        ).save(
                loginCode
        );
    }
}