package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.AuthenticationDTO;
import com.evomanex.AppRH.domain.user.EmailConfirmationRequestDTO;
import com.evomanex.AppRH.domain.user.EmailResendRequestDTO;
import com.evomanex.AppRH.domain.user.MessageResponseDTO;
import com.evomanex.AppRH.domain.user.OAuthCodeExchangeResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.service.AuthenticationCompletionService;
import com.evomanex.AppRH.service.EmailVerificationService;
import com.evomanex.AppRH.service.PublicRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationCompletionService
            authenticationCompletionService;

    @Mock
    private PublicRegistrationService
            publicRegistrationService;

    @Mock
    private EmailVerificationService
            emailVerificationService;

    @Mock
    private Authentication authentication;

    @Mock
    private User user;

    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        controller =
                new AuthenticationController(
                        authenticationManager,
                        authenticationCompletionService,
                        publicRegistrationService,
                        emailVerificationService
                );
    }

    @Test
    void shouldNormalizeLoginBeforeAuthentication() {
        AuthenticationDTO request =
                new AuthenticationDTO(
                        "  USUARIO@EXAMPLE.COM  ",
                        "senha"
                );

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenReturn(
                authentication
        );

        when(
                authentication.getPrincipal()
        ).thenReturn(
                user
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
                authenticationCompletionService.complete(
                        user
                )
        ).thenReturn(
                expectedResponse
        );

        controller.login(
                request
        );

        ArgumentCaptor<UsernamePasswordAuthenticationToken>
                authenticationCaptor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(
                authenticationManager
        ).authenticate(
                authenticationCaptor.capture()
        );

        UsernamePasswordAuthenticationToken capturedToken =
                authenticationCaptor.getValue();

        assertThat(
                capturedToken.getPrincipal()
        ).isEqualTo(
                "usuario@example.com"
        );

        assertThat(
                capturedToken.getCredentials()
        ).isEqualTo(
                "senha"
        );

        verify(
                authenticationCompletionService
        ).complete(
                user
        );
    }

    @Test
    void shouldReturnAccessTokenWhenMfaIsDisabled() {
        AuthenticationDTO request =
                new AuthenticationDTO(
                        "usuario@example.com",
                        "senha"
                );

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenReturn(
                authentication
        );

        when(
                authentication.getPrincipal()
        ).thenReturn(
                user
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
                authenticationCompletionService.complete(
                        user
                )
        ).thenReturn(
                expectedResponse
        );

        ResponseEntity<OAuthCodeExchangeResponseDTO> result =
                controller.login(
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        OAuthCodeExchangeResponseDTO body =
                result.getBody();

        assertThat(
                body
        ).isNotNull();

        assertThat(
                body.mfaRequired()
        ).isFalse();

        assertThat(
                body.challengeToken()
        ).isNull();

        assertThat(
                body.accessToken()
        ).isEqualTo(
                "jwt-access-token"
        );

        assertThat(
                body.tokenType()
        ).isEqualTo(
                "Bearer"
        );

        assertThat(
                body.expiresIn()
        ).isEqualTo(
                7200L
        );

        verify(
                authenticationCompletionService
        ).complete(
                user
        );
    }

    @Test
    void shouldReturnChallengeWithoutJwtWhenMfaIsEnabled() {
        AuthenticationDTO request =
                new AuthenticationDTO(
                        "usuario@example.com",
                        "senha"
                );

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenReturn(
                authentication
        );

        when(
                authentication.getPrincipal()
        ).thenReturn(
                user
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
                authenticationCompletionService.complete(
                        user
                )
        ).thenReturn(
                expectedResponse
        );

        ResponseEntity<OAuthCodeExchangeResponseDTO> result =
                controller.login(
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        OAuthCodeExchangeResponseDTO body =
                result.getBody();

        assertThat(
                body
        ).isNotNull();

        assertThat(
                body.mfaRequired()
        ).isTrue();

        assertThat(
                body.challengeToken()
        ).isEqualTo(
                "mfa-challenge-token"
        );

        assertThat(
                body.accessToken()
        ).isNull();

        assertThat(
                body.tokenType()
        ).isNull();

        assertThat(
                body.expiresIn()
        ).isNull();

        verify(
                authenticationCompletionService
        ).complete(
                user
        );
    }

    @Test
    void shouldRejectInvalidAuthenticatedPrincipal() {
        AuthenticationDTO request =
                new AuthenticationDTO(
                        "usuario@example.com",
                        "senha"
                );

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenReturn(
                authentication
        );

        when(
                authentication.getPrincipal()
        ).thenReturn(
                "invalid-principal"
        );

        assertThatThrownBy(
                () ->
                        controller.login(
                                request
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O usuário autenticado é inválido."
                );

        verify(
                authenticationCompletionService,
                never()
        ).complete(
                any(
                        User.class
                )
        );
    }

    @Test
    void shouldPropagateAuthenticationFailure() {
        AuthenticationDTO request =
                new AuthenticationDTO(
                        "usuario@example.com",
                        "senha-incorreta"
                );

        IllegalStateException authenticationFailure =
                new IllegalStateException(
                        "Credenciais inválidas."
                );

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenThrow(
                authenticationFailure
        );

        assertThatThrownBy(
                () ->
                        controller.login(
                                request
                        )
        ).isSameAs(
                authenticationFailure
        );

        verify(
                authenticationCompletionService,
                never()
        ).complete(
                any(
                        User.class
                )
        );
    }

    @Test
    void shouldConfirmEmail() {
        EmailConfirmationRequestDTO request =
                new EmailConfirmationRequestDTO(
                        "email-confirmation-token"
                );

        ResponseEntity<MessageResponseDTO> result =
                controller.confirmEmail(
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().message()
        ).isEqualTo(
                "E-mail confirmado com sucesso"
        );

        verify(
                emailVerificationService
        ).confirmEmail(
                "email-confirmation-token"
        );
    }

    @Test
    void shouldResendEmailConfirmation() {
        EmailResendRequestDTO request =
                new EmailResendRequestDTO(
                        "usuario@example.com"
                );

        ResponseEntity<MessageResponseDTO> result =
                controller.resendEmailConfirmation(
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().message()
        ).isEqualTo(
                "Se a conta existir e ainda estiver pendente, "
                        + "um novo e-mail de confirmação será enviado"
        );

        verify(
                emailVerificationService
        ).resendVerification(
                "usuario@example.com"
        );
    }
}