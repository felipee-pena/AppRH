package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Dados para cadastro de um candidato"
)
public record RegisterDTO(

        @NotBlank(
                message = "Informe o login"
        )
        @Schema(
                description = "Login do candidato",
                example = "candidato@apprh.local"
        )
        String login,

        @NotBlank(
                message = "Informe a senha"
        )
        @Size(
                min = 8,
                message = "A senha deve possuir pelo menos 8 caracteres"
        )
        @Schema(
                description = "Senha do candidato",
                example = "Candidato@123"
        )
        String password

) {
}
