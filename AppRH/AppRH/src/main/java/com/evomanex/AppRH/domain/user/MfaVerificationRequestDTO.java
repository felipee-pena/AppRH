package com.evomanex.AppRH.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaVerificationRequestDTO(

        @NotBlank(
                message = "O desafio MFA é obrigatório."
        )
        @JsonProperty(
                "challenge_token"
        )
        String challengeToken,

        @NotBlank(
                message = "O código MFA é obrigatório."
        )
        @Pattern(
                regexp = "\\d{6}",
                message = "O código MFA deve possuir 6 dígitos."
        )
        String code

) {
}