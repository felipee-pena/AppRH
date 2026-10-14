package com.evomanex.AppRH.domain.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import com.evomanex.AppRH.domain.user.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(
                fieldError -> errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), "Dados inválidos", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        String parameter = exception.getName();

        String message;

        if ("code".equals(parameter)) {
            message = "O código da vaga deve ser numérico";
        } else {
            message = "O valor informado possui formato inválido";
        }

        ApiErrorResponseDTO response = new ApiErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(), "Parâmetro inválido", Map.of(parameter, message));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        String allowedMethods = exception.getSupportedHttpMethods() == null
                ? "Nenhum método informado"
                : exception.getSupportedHttpMethods()
                .stream()
                .map(HttpMethod::name)
                .collect(Collectors.joining(", "));

        ApiErrorResponseDTO response = new ApiErrorResponseDTO(HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Método HTTP não permitido para este endpoint",
                Map.of("method", exception.getMethod(), "allowedMethods", allowedMethods));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleResourceNotFound(
            NoResourceFoundException exception) {
        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(HttpStatus.NOT_FOUND.value(), "Endpoint não encontrado", Map.of());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAccessDenied(AccessDeniedException exception) {
        String message = exception.getMessage() == null
                ? "Usuário não possui permissão para esta operação"
                : exception.getMessage();

        ApiErrorResponseDTO response =
                new ApiErrorResponseDTO(HttpStatus.FORBIDDEN.value(), message, Map.of());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}