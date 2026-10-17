package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro público de candidato ou profissional de RH")
public record RegisterDTO(
        @Schema(description = "Login do usuário", example = "usuario@apprh.local")
        @NotBlank(message = "Informe o login")
        String login,

        @Schema(description = "Senha do usuário", example = "Usuario@123")
        @NotBlank(message = "Informe a senha")
        @Size(min = 8, message = "A senha deve possuir pelo menos 8 caracteres")
        String password,

        @Schema(description = "Perfil público escolhido", example = "USER")
        @NotNull(message = "Informe o perfil")
        PublicRegistrationRole role,

        @Schema(description = "Empresa existente selecionada para USERRH", example = "1", nullable = true) Long companyId,

        @Schema(description = "Nome de uma nova empresa para USERRH", example = "Claro", nullable = true)
        @Size(max = 150, message = "O nome da empresa deve possuir no máximo 150 caracteres")
        String newCompanyName
) {}
