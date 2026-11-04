package com.evomanex.AppRH.domain.user;

import jakarta.validation.constraints.NotBlank;

public record OAuthCodeExchangeRequestDTO(

        @NotBlank(
                message = "O código OAuth é obrigatório."
        )
        String code

) {
}