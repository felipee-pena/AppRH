package com.evomanex.AppRH.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Usuário cadastrado com sucesso"
)
public record RegisterResponseDTO(

        @Schema(
                description = "Identificador do usuário"
        )
        String id,

        @Schema(
                description = "Login cadastrado",
                example = "candidato@apprh.local"
        )
        String login,

        @Schema(
                description = "Perfil atribuído ao cadastro",
                example = "USER"
        )
        UserRole role

) {

    public static RegisterResponseDTO from(
            User user
    ) {
        return new RegisterResponseDTO(
                user.getId(),
                user.getLogin(),
                user.getRole()
        );
    }
}