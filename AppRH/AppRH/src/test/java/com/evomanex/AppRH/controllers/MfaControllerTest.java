package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.MfaVerificationRequestDTO;
import com.evomanex.AppRH.domain.user.OAuthCodeExchangeResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.TokenService;
import com.evomanex.AppRH.infra.security.mfa.MfaChallengeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaControllerTest {

    @Mock
    private MfaChallengeService mfaChallengeService;

    @Mock
    private TokenService tokenService;

    @Mock
    private User user;

    private MfaController controller;

    @BeforeEach
    void setUp() {
        controller =
                new MfaController(
                        mfaChallengeService,
                        tokenService
                );
    }

    @Test
    void shouldVerifyMfaAndReturnAccessToken() {
        MfaVerificationRequestDTO request =
                new MfaVerificationRequestDTO(
                        "mfa-challenge-token",
                        "123456"
                );

        when(
                mfaChallengeService.verifyTotp(
                        "mfa-challenge-token",
                        "123456"
                )
        ).thenReturn(
                user
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

        ResponseEntity<OAuthCodeExchangeResponseDTO> result =
                controller.verify(
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().mfaRequired()
        ).isFalse();

        assertThat(
                result.getBody().challengeToken()
        ).isNull();

        assertThat(
                result.getBody().accessToken()
        ).isEqualTo(
                "jwt-access-token"
        );

        assertThat(
                result.getBody().tokenType()
        ).isEqualTo(
                "Bearer"
        );

        assertThat(
                result.getBody().expiresIn()
        ).isEqualTo(
                7200L
        );

        verify(
                mfaChallengeService
        ).verifyTotp(
                "mfa-challenge-token",
                "123456"
        );

        verify(
                tokenService
        ).generateToken(
                user
        );

        verify(
                tokenService
        ).getExpirationSeconds();
    }

    @Test
    void shouldGenerateTokenForUserReturnedByChallengeService() {
        MfaVerificationRequestDTO request =
                new MfaVerificationRequestDTO(
                        "challenge",
                        "654321"
                );

        when(
                mfaChallengeService.verifyTotp(
                        "challenge",
                        "654321"
                )
        ).thenReturn(
                user
        );

        when(
                tokenService.generateToken(
                        user
                )
        ).thenReturn(
                "generated-token"
        );

        when(
                tokenService.getExpirationSeconds()
        ).thenReturn(
                3600L
        );

        ResponseEntity<OAuthCodeExchangeResponseDTO> result =
                controller.verify(
                        request
                );

        OAuthCodeExchangeResponseDTO body =
                result.getBody();

        assertThat(
                body
        ).isNotNull();

        assertThat(
                body.accessToken()
        ).isEqualTo(
                "generated-token"
        );

        assertThat(
                body.expiresIn()
        ).isEqualTo(
                3600L
        );
    }
}