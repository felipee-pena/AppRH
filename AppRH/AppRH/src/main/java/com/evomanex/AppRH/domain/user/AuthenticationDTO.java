package com.evomanex.AppRH.domain.user;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
        @NotBlank(message = "Informe o login")
        String login,

        @NotBlank(message = "Informe a senha")
        String password
) {
}