package com.evomanex.AppRH.domain.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaDisableRequestDTO(

        @NotBlank(
                message = "A senha atual é obrigatória."
        )
        String password,

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