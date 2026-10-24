package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.AuthenticationDTO;
import com.evomanex.AppRH.domain.user.EmailConfirmationRequestDTO;
import com.evomanex.AppRH.domain.user.EmailResendRequestDTO;
import com.evomanex.AppRH.domain.user.GlobalExceptionHandler;
import com.evomanex.AppRH.domain.user.PublicRegistrationRole;
import com.evomanex.AppRH.domain.user.RegisterDTO;
import com.evomanex.AppRH.domain.user.RegisterResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import com.evomanex.AppRH.infra.security.TokenService;

import com.evomanex.AppRH.service.EmailVerificationService;
import com.evomanex.AppRH.service.PublicRegistrationService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private PublicRegistrationService publicRegistrationService;

    @Mock
    private EmailVerificationService emailVerificationService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuthenticationController controller =
                new AuthenticationController(
                        authenticationManager,
                        tokenService,
                        publicRegistrationService,
                        emailVerificationService
                );

        objectMapper =
                new ObjectMapper();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                controller
                        )
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();
    }

    @Test
    void shouldRegisterValidCandidate() throws Exception {
        RegisterDTO request =
                new RegisterDTO(
                        "candidato@example.com",
                        "Usuario@123",
                        PublicRegistrationRole.USER,
                        null,
                        null
                );

        RegisterResponseDTO response =
                new RegisterResponseDTO(
                        "user-id",
                        "candidato@example.com",
                        UserRole.USER
                );

        when(
                publicRegistrationService.register(
                        any(RegisterDTO.class)
                )
        ).thenReturn(
                response
        );

        mockMvc.perform(
                        post(
                                "/auth/register"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.id")
                                .value("user-id")
                )
                .andExpect(
                        jsonPath("$.login")
                                .value("candidato@example.com")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("USER")
                );

        verify(
                publicRegistrationService
        ).register(
                any(RegisterDTO.class)
        );
    }

    @Test
    void shouldRejectInvalidRegistrationPayload() throws Exception {
        RegisterDTO request =
                new RegisterDTO(
                        "email-invalido",
                        "123",
                        null,
                        null,
                        null
                );

        mockMvc.perform(
                        post(
                                "/auth/register"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Dados inválidos")
                )
                .andExpect(
                        jsonPath("$.errors.login")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.errors.password")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.errors.role")
                                .exists()
                );
    }

    @Test
    void shouldLoginAndReturnJwt() throws Exception {
        AuthenticationDTO request =
                new AuthenticationDTO(
                        "  Usuario@Example.COM  ",
                        "Usuario@123"
                );

        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
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
                tokenService.generateToken(
                        user
                )
        ).thenReturn(
                "jwt-token"
        );

        mockMvc.perform(
                        post(
                                "/auth/login"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.access_token")
                                .value("jwt-token")
                )
                .andExpect(
                        jsonPath("$.token_type")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expires_in")
                                .value(
                                        TokenService.EXPIRATION_SECONDS
                                )
                );
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {
        AuthenticationDTO request =
                new AuthenticationDTO(
                        "usuario@example.com",
                        "senha-incorreta"
                );

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenThrow(
                new BadCredentialsException(
                        "Credenciais inválidas"
                )
        );

        mockMvc.perform(
                        post(
                                "/auth/login"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Login ou senha inválidos")
                );
    }

    @Test
    void shouldConfirmEmail() throws Exception {
        EmailConfirmationRequestDTO request =
                new EmailConfirmationRequestDTO(
                        "token-valido"
                );

        doNothing()
                .when(
                        emailVerificationService
                )
                .confirmEmail(
                        "token-valido"
                );

        mockMvc.perform(
                        post(
                                "/auth/email/confirm"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "E-mail confirmado com sucesso"
                                )
                );

        verify(
                emailVerificationService
        ).confirmEmail(
                "token-valido"
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidConfirmationToken()
            throws Exception {
        EmailConfirmationRequestDTO request =
                new EmailConfirmationRequestDTO(
                        "token-invalido"
                );

        doThrow(
                new ResponseStatusException(
                        BAD_REQUEST,
                        "Token de confirmação inválido"
                )
        )
                .when(
                        emailVerificationService
                )
                .confirmEmail(
                        "token-invalido"
                );

        mockMvc.perform(
                        post(
                                "/auth/email/confirm"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Token de confirmação inválido"
                                )
                );
    }

    @Test
    void shouldReturnGenericMessageWhenResendingConfirmation()
            throws Exception {
        EmailResendRequestDTO request =
                new EmailResendRequestDTO(
                        "usuario@example.com"
                );

        doNothing()
                .when(
                        emailVerificationService
                )
                .resendVerification(
                        "usuario@example.com"
                );

        mockMvc.perform(
                        post(
                                "/auth/email/resend"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Se a conta existir e ainda estiver pendente, "
                                                + "um novo e-mail de confirmação será enviado"
                                )
                );

        verify(
                emailVerificationService
        ).resendVerification(
                "usuario@example.com"
        );
    }

    @Test
    void shouldRejectInvalidEmailWhenResendingConfirmation()
            throws Exception {
        EmailResendRequestDTO request =
                new EmailResendRequestDTO(
                        "email-invalido"
                );

        mockMvc.perform(
                        post(
                                "/auth/email/resend"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.errors.login")
                                .value(
                                        "Informe um e-mail válido"
                                )
                );
    }
}