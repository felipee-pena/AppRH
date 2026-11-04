package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.OAuthCodeExchangeRequestDTO;
import com.evomanex.AppRH.domain.user.OAuthCodeExchangeResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.service.AuthenticationCompletionService;
import com.evomanex.AppRH.service.OAuthLoginCodeService;
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
class OAuthExchangeControllerTest {

    @Mock
    private OAuthLoginCodeService loginCodeService;

    @Mock
    private AuthenticationCompletionService
            authenticationCompletionService;

    @Mock
    private User user;

    private OAuthExchangeController controller;

    @BeforeEach
    void setUp() {
        controller =
                new OAuthExchangeController(
                        loginCodeService,
                        authenticationCompletionService
                );
    }

    @Test
    void shouldExchangeOAuthCodeForAccessToken() {
        OAuthCodeExchangeRequestDTO request =
                new OAuthCodeExchangeRequestDTO(
                        "temporary-oauth-code"
                );

        OAuthCodeExchangeResponseDTO expectedResponse =
                new OAuthCodeExchangeResponseDTO(
                        false,
                        null,
                        "jwt-access-token",
                        "Bearer",
                        7200L
                );

        when(
                loginCodeService.consumeCode(
                        "temporary-oauth-code"
                )
        ).thenReturn(
                user
        );

        when(
                authenticationCompletionService.complete(
                        user
                )
        ).thenReturn(
                expectedResponse
        );

        ResponseEntity<OAuthCodeExchangeResponseDTO> result =
                controller.exchange(
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isSameAs(
                expectedResponse
        );

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().mfaRequired()
        ).isFalse();

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
                loginCodeService
        ).consumeCode(
                "temporary-oauth-code"
        );

        verify(
                authenticationCompletionService
        ).complete(
                user
        );
    }

    @Test
    void shouldReturnMfaChallengeWhenMfaIsEnabled() {
        OAuthCodeExchangeRequestDTO request =
                new OAuthCodeExchangeRequestDTO(
                        "temporary-oauth-code"
                );

        OAuthCodeExchangeResponseDTO expectedResponse =
                new OAuthCodeExchangeResponseDTO(
                        true,
                        "mfa-challenge-token",
                        null,
                        null,
                        null
                );

        when(
                loginCodeService.consumeCode(
                        "temporary-oauth-code"
                )
        ).thenReturn(
                user
        );

        when(
                authenticationCompletionService.complete(
                        user
                )
        ).thenReturn(
                expectedResponse
        );

        ResponseEntity<OAuthCodeExchangeResponseDTO> result =
                controller.exchange(
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
        ).isTrue();

        assertThat(
                result.getBody().challengeToken()
        ).isEqualTo(
                "mfa-challenge-token"
        );

        assertThat(
                result.getBody().accessToken()
        ).isNull();

        assertThat(
                result.getBody().tokenType()
        ).isNull();

        assertThat(
                result.getBody().expiresIn()
        ).isNull();
    }
}