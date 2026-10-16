package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Dados para localizar ou criar a empresa do usuário de RH"
)
public record CompanyAssociationRequestDTO(

        @Schema(
                description = "Nome da empresa",
                example = "Claro"
        )
        @NotBlank(
                message = "Informe o nome da empresa"
        )
        @Size(
                max = 150,
                message = "O nome da empresa deve possuir no máximo 150 caracteres"
        )
        String name

) {
}