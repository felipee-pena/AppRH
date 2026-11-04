package com.evomanex.AppRH.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MfaSetupResponseDTO(

        String secret,

        @JsonProperty("otpauth_uri")
        String otpAuthUri,

        @JsonProperty("recovery_codes")
        List<String> recoveryCodes

) {

    public MfaSetupResponseDTO {
        recoveryCodes =
                recoveryCodes == null
                        ? List.of()
                        : List.copyOf(
                        recoveryCodes
                );
    }
}