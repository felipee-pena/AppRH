package com.evomanex.AppRH.domain.user;

import com.evomanex.AppRH.infra.security.oauth.OAuthAccountLinkException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(
            DisabledException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleDisabledAccount(
            DisabledException exception
    ) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Confirme seu e-mail ou aguarde "
                                + "a aprovação da conta",
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.UNAUTHORIZED
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            LockedException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleLockedAccount(
            LockedException exception
    ) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.FORBIDDEN.value(),
                        "A conta está bloqueada",
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.FORBIDDEN
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            BadCredentialsException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleBadCredentials(
            BadCredentialsException exception
    ) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Login ou senha inválidos",
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.UNAUTHORIZED
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            OAuthAccountLinkException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleOAuthAccountLinkException(
            OAuthAccountLinkException exception
    ) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        exception.getMessage(),
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            OAuth2AuthenticationException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleOAuth2AuthenticationException(
            OAuth2AuthenticationException exception
    ) {
        LOGGER.warn(
                "Falha na autenticação OAuth: {}",
                exception.getMessage()
        );

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Não foi possível autenticar "
                                + "com o provedor selecionado",
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.UNAUTHORIZED
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors =
                new LinkedHashMap<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(
                        fieldError ->
                                errors.putIfAbsent(
                                        fieldError.getField(),
                                        fieldError.getDefaultMessage()
                                )
                );

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dados inválidos",
                        errors
                );

        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        Map<String, String> errors =
                new LinkedHashMap<>();

        exception
                .getConstraintViolations()
                .forEach(
                        violation ->
                                errors.putIfAbsent(
                                        violation
                                                .getPropertyPath()
                                                .toString(),
                                        violation.getMessage()
                                )
                );

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        "Dados inválidos",
                        errors
                );

        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception
    ) {
        LOGGER.warn(
                "JSON inválido recebido: {}",
                exception.getMessage()
        );

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        "O corpo da requisição possui "
                                + "JSON inválido",
                        Map.of(
                                "requestBody",
                                "Revise os campos, tipos, "
                                        + "vírgulas e aspas "
                                        + "do JSON enviado"
                        )
                );

        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        String parameter =
                exception.getName();

        String message;

        if ("code".equals(parameter)) {
            message =
                    "O código deve ser numérico";
        } else if (
                "id".equals(parameter)
                        || "companyId".equals(parameter)
        ) {
            message =
                    "O identificador possui formato inválido";
        } else {
            message =
                    "O valor informado possui formato inválido";
        }

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        "Parâmetro inválido",
                        Map.of(
                                parameter,
                                message
                        )
                );

        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            HttpRequestMethodNotSupportedException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        String allowedMethods =
                exception.getSupportedHttpMethods()
                        == null
                        ? "Nenhum método informado"
                        : exception
                        .getSupportedHttpMethods()
                        .stream()
                        .map(
                                HttpMethod::name
                        )
                        .collect(
                                Collectors.joining(
                                        ", "
                                )
                        );

        Map<String, String> errors =
                new LinkedHashMap<>();

        errors.put(
                "method",
                exception.getMethod()
        );

        errors.put(
                "allowedMethods",
                allowedMethods
        );

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus
                                .METHOD_NOT_ALLOWED
                                .value(),
                        "Método HTTP não permitido "
                                + "para este endpoint",
                        errors
                );

        return ResponseEntity
                .status(
                        HttpStatus.METHOD_NOT_ALLOWED
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            NoResourceFoundException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleResourceNotFound(
            NoResourceFoundException exception
    ) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.NOT_FOUND.value(),
                        "Endpoint não encontrado",
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.NOT_FOUND
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            ResponseStatusException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleResponseStatusException(
            ResponseStatusException exception
    ) {
        String message =
                exception.getReason() == null
                        ? "Não foi possível processar "
                        + "a solicitação"
                        : exception.getReason();

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        exception
                                .getStatusCode()
                                .value(),
                        message,
                        Map.of()
                );

        return ResponseEntity
                .status(
                        exception.getStatusCode()
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            AccessDeniedException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleAccessDenied(
            AccessDeniedException exception
    ) {
        String message =
                exception.getMessage() == null
                        || exception
                        .getMessage()
                        .isBlank()
                        ? "Usuário não possui permissão "
                        + "para esta operação"
                        : exception.getMessage();

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.FORBIDDEN.value(),
                        message,
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.FORBIDDEN
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        LOGGER.error(
                "Violação de integridade "
                        + "ao processar requisição",
                exception
        );

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        "A operação viola uma regra "
                                + "de integridade dos dados",
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.BAD_REQUEST
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            IllegalStateException.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleIllegalStateException(
            IllegalStateException exception
    ) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus.CONFLICT.value(),
                        exception.getMessage(),
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.CONFLICT
                )
                .body(
                        response
                );
    }

    @ExceptionHandler(
            Exception.class
    )
    public ResponseEntity<ApiErrorResponseDTO>
    handleUnexpectedException(
            Exception exception
    ) {
        LOGGER.error(
                "Erro interno inesperado. "
                        + "Tipo: {}. Mensagem: {}",
                exception
                        .getClass()
                        .getName(),
                exception.getMessage(),
                exception
        );

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(
                        HttpStatus
                                .INTERNAL_SERVER_ERROR
                                .value(),
                        "Ocorreu um erro interno inesperado",
                        Map.of()
                );

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        response
                );
    }
}