package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.OAuthCodeExchangeResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.TokenService;
import com.evomanex.AppRH.infra.security.mfa.MfaChallengeService;
import com.evomanex.AppRH.repository.UserMfaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationCompletionServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserMfaRepository userMfaRepository;

    @Mock
    private MfaChallengeService mfaChallengeService;

    @Mock
    private User user;

    private AuthenticationCompletionService service;

    @BeforeEach
    void setUp() {
        service =
                new AuthenticationCompletionService(
                        tokenService,
                        userMfaRepository,
                        mfaChallengeService
                );
    }

    @Test
    void shouldGenerateJwtWhenMfaIsDisabled() {
        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                userMfaRepository
                        .existsByUser_IdAndEnabledTrue(
                                "user-uuid-123"
                        )
        ).thenReturn(
                false
        );

        when(
                tokenService.generateToken(
                        user
                )
        ).thenReturn(
                "jwt-access-token"
        );

        when(
                tokenService.getExpirationSeconds()
        ).thenReturn(
                7200L
        );

        OAuthCodeExchangeResponseDTO response =
                service.complete(
                        user
                );

        assertThat(
                response.mfaRequired()
        ).isFalse();

        assertThat(
                response.challengeToken()
        ).isNull();

        assertThat(
                response.accessToken()
        ).isEqualTo(
                "jwt-access-token"
        );

        assertThat(
                response.tokenType()
        ).isEqualTo(
                "Bearer"
        );

        assertThat(
                response.expiresIn()
        ).isEqualTo(
                7200L
        );

        verify(
                mfaChallengeService,
                never()
        ).createChallenge(
                user
        );
    }

    @Test
    void shouldGenerateChallengeWhenMfaIsEnabled() {
        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                userMfaRepository
                        .existsByUser_IdAndEnabledTrue(
                                "user-uuid-123"
                        )
        ).thenReturn(
                true
        );

        when(
                mfaChallengeService.createChallenge(
                        user
                )
        ).thenReturn(
                "mfa-challenge-token"
        );

        OAuthCodeExchangeResponseDTO response =
                service.complete(
                        user
                );

        assertThat(
                response.mfaRequired()
        ).isTrue();

        assertThat(
                response.challengeToken()
        ).isEqualTo(
                "mfa-challenge-token"
        );

        assertThat(
                response.accessToken()
        ).isNull();

        assertThat(
                response.tokenType()
        ).isNull();

        assertThat(
                response.expiresIn()
        ).isNull();

        verify(
                tokenService,
                never()
        ).generateToken(
                user
        );
    }

    @Test
    void shouldRejectInactiveUser() {
        when(
                user.isEnabled()
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        service.complete(
                                user
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "A conta AppRH não está ativa."
                );

        verify(
                userMfaRepository,
                never()
        ).existsByUser_IdAndEnabledTrue(
                org.mockito.ArgumentMatchers
                        .anyString()
        );

        verify(
                tokenService,
                never()
        ).generateToken(
                user
        );

        verify(
                mfaChallengeService,
                never()
        ).createChallenge(
                user
        );
    }
}