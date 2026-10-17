package com.evomanex.AppRH.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailResendRequestDTO(

        @NotBlank(
                message = "Informe o login"
        )
        @Email(
                message = "Informe um e-mail válido"
        )
        String login

) {
}