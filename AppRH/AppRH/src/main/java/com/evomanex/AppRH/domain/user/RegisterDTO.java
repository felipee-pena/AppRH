package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Dados para cadastro público de candidato ou profissional de RH"
)
public record RegisterDTO(

        @Schema(
                description = "E-mail utilizado como login",
                example = "usuario@empresa.com.br"
        )
        @NotBlank(
                message = "Informe o login"
        )
        @Email(
                message = "Informe um e-mail válido"
        )
        @Size(
                max = 255,
                message = "O login deve possuir no máximo 255 caracteres"
        )
        String login,

        @Schema(
                description = "Senha do usuário",
                example = "Usuario@123"
        )
        @NotBlank(
                message = "Informe a senha"
        )
        @Size(
                min = 8,
                max = 72,
                message = "A senha deve possuir entre 8 e 72 caracteres"
        )
        String password,

        @Schema(
                description = "Perfil público escolhido",
                example = "USER"
        )
        @NotNull(
                message = "Informe o perfil"
        )
        PublicRegistrationRole role,

        @Schema(
                description = "Empresa existente selecionada para USERRH",
                example = "1",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        Long companyId,

        @Schema(
                description = "Nome de uma nova empresa para USERRH",
                example = "Claro",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(
                max = 150,
                message = "O nome da empresa deve possuir no máximo 150 caracteres"
        )
        String newCompanyName

) {
}