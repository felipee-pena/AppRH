package com.evomanex.AppRH.domain.user;

import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleDisabledAccount() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleDisabledAccount(
                        new DisabledException(
                                "Conta desabilitada"
                        )
                );

        assertResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Confirme seu e-mail ou aguarde a aprovação da conta"
        );
    }

    @Test
    void shouldHandleLockedAccount() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleLockedAccount(
                        new LockedException(
                                "Conta bloqueada"
                        )
                );

        assertResponse(
                response,
                HttpStatus.FORBIDDEN,
                "A conta está bloqueada"
        );
    }

    @Test
    void shouldHandleBadCredentials() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleBadCredentials(
                        new BadCredentialsException(
                                "Credenciais inválidas"
                        )
                );

        assertResponse(
                response,
                HttpStatus.UNAUTHORIZED,
                "Login ou senha inválidos"
        );
    }

    @Test
    void shouldHandleResponseStatusException() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleResponseStatusException(
                        new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Registro duplicado"
                        )
                );

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "Registro duplicado"
        );
    }

    @Test
    void shouldUseGenericMessageWhenResponseStatusHasNoReason() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleResponseStatusException(
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST
                        )
                );

        assertResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "Não foi possível processar a solicitação"
        );
    }

    @Test
    void shouldHandleAccessDenied() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleAccessDenied(
                        new AccessDeniedException(
                                "Acesso administrativo necessário"
                        )
                );

        assertResponse(
                response,
                HttpStatus.FORBIDDEN,
                "Acesso administrativo necessário"
        );
    }

    @Test
    void shouldUseGenericMessageForAccessDeniedWithoutMessage() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleAccessDenied(
                        new AccessDeniedException(
                                ""
                        )
                );

        assertResponse(
                response,
                HttpStatus.FORBIDDEN,
                "Usuário não possui permissão para esta operação"
        );
    }

    @Test
    void shouldHandleDataIntegrityViolation() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleDataIntegrityViolation(
                        new DataIntegrityViolationException(
                                "Constraint violation"
                        )
                );

        assertResponse(
                response,
                HttpStatus.CONFLICT,
                "A operação viola uma regra de integridade dos dados"
        );
    }

    @Test
    void shouldHandleUnexpectedException() {
        ResponseEntity<ApiErrorResponseDTO> response =
                handler.handleUnexpectedException(
                        new IllegalStateException(
                                "Erro de teste"
                        )
                );

        assertResponse(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado"
        );
    }

    private static void assertResponse(
            ResponseEntity<ApiErrorResponseDTO> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                expectedStatus
        );

        assertThat(
                response.getBody()
        ).isNotNull();

        assertThat(
                response.getBody().status()
        ).isEqualTo(
                expectedStatus.value()
        );

        assertThat(
                response.getBody().message()
        ).isEqualTo(
                expectedMessage
        );

        assertThat(
                response.getBody().errors()
        ).isEmpty();
    }
}