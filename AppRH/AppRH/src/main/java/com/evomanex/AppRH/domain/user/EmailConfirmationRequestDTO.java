package com.evomanex.AppRH.domain.user;

import jakarta.validation.constraints.NotBlank;

public record EmailConfirmationRequestDTO(

        @NotBlank(
                message = "Informe o token"
        )
        String token

) {
}