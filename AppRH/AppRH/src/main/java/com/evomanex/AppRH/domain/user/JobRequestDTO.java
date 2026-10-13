package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessários para cadastrar ou atualizar uma vaga")
public record JobRequestDTO(

        @Schema(
                description = "Nome da vaga",
                example = "Analista de Sistemas"
        )
        @NotBlank(message = "Informe o nome da vaga")
        String name,

        @Schema(
                description = "Descrição das responsabilidades e requisitos",
                example = "Desenvolvimento de aplicações com Java e Spring Boot"
        )
        @NotBlank(message = "Informe a descrição")
        String description,

        @Schema(
                description = "Data de expiração da vaga",
                example = "2026-12-31"
        )
        @NotBlank(message = "Informe a data")
        String date,

        @Schema(
                description = "Salário oferecido",
                example = "5500.00"
        )
        @NotBlank(message = "Informe o salário")
        String salary

) {
}