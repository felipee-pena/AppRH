package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
        description = "Dados necessários para cadastrar ou atualizar uma vaga"
)
public record JobRequestDTO(

        @Schema(
                description = "Nome da vaga",
                example = "Analista de Sistemas"
        )
        @NotBlank(
                message = "Informe o nome da vaga"
        )
        @Size(
                max = 255,
                message = "O nome deve possuir no máximo 255 caracteres"
        )
        String name,

        @Schema(
                description = "Descrição das responsabilidades e requisitos",
                example = "Desenvolvimento de aplicações com Java e Spring Boot"
        )
        @NotBlank(
                message = "Informe a descrição"
        )
        @Size(
                max = 5000,
                message = "A descrição deve possuir no máximo 5000 caracteres"
        )
        String description,

        @Schema(
                description = "Data de expiração da vaga",
                example = "2027-12-31"
        )
        @NotNull(
                message = "Informe a data de expiração"
        )
        @FutureOrPresent(
                message = "A data de expiração deve ser atual ou futura"
        )
        LocalDate expirationDate,

        @Schema(
                description = "Salário oferecido",
                example = "9500.00"
        )
        @NotNull(
                message = "Informe o salário"
        )
        @DecimalMin(
                value = "0.00",
                message = "O salário não pode ser negativo"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "O salário deve possuir no máximo duas casas decimais"
        )
        BigDecimal salary,

        @Schema(
                description = "Empresa escolhida pelo ADMIN. Para USERRH, a empresa associada ao perfil será utilizada.",
                example = "1",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Long companyId,

        @Schema(
                description = "E-mail opcional para contato",
                example = "vagas@empresa.com.br",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Email(
                message = "Informe um e-mail válido"
        )
        @Size(
                max = 255,
                message = "O e-mail deve possuir no máximo 255 caracteres"
        )
        String contactEmail

) {
}