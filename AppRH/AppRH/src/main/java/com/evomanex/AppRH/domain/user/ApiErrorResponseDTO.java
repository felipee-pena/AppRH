package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(
        description = "Resposta retornada quando ocorre um erro de validação"
)
public record ApiErrorResponseDTO(

        @Schema(
                description = "Código HTTP do erro",
                example = "400"
        )
        int status,

        @Schema(
                description = "Descrição geral do erro",
                example = "Dados inválidos"
        )
        String message,

        @Schema(
                description = "Erros de validação organizados pelo nome do campo"
        )
        Map<String, String> errors

) {
}