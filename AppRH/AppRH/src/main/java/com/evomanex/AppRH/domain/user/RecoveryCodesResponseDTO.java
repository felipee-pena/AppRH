package com.evomanex.AppRH.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecoveryCodesResponseDTO(

        @JsonProperty(
                "recovery_codes"
        )
        List<String> recoveryCodes

) {

    public RecoveryCodesResponseDTO {
        recoveryCodes =
                recoveryCodes == null
                        ? List.of()
                        : List.copyOf(
                        recoveryCodes
                );
    }
}