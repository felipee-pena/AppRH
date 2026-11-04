package com.evomanex.AppRH.domain.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(
        JsonInclude.Include.NON_NULL
)
public record OAuthCodeExchangeResponseDTO(

        @JsonProperty(
                "mfa_required"
        )
        boolean mfaRequired,

        @JsonProperty(
                "challenge_token"
        )
        String challengeToken,

        @JsonProperty(
                "access_token"
        )
        String accessToken,

        @JsonProperty(
                "token_type"
        )
        String tokenType,

        @JsonProperty(
                "expires_in"
        )
        Long expiresIn

) {
}