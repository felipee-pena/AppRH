package com.evomanex.AppRH.domain.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfaStatusResponseDTO(

        @JsonProperty(
                "mfa_enabled"
        )
        boolean mfaEnabled

) {
}