package com.evomanex.AppRH.infra.security.oauth;

import java.util.Locale;

public enum ExternalIdentityProvider {

    GOOGLE,
    GITHUB;

    public static ExternalIdentityProvider
    fromRegistrationId(
            String registrationId
    ) {
        if (
                registrationId == null
                        || registrationId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "O identificador do provedor "
                            + "é obrigatório."
            );
        }

        return switch (
                registrationId
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        )
                ) {
            case "google" ->
                    GOOGLE;

            case "github" ->
                    GITHUB;

            default ->
                    throw new IllegalArgumentException(
                            "Provedor OAuth não suportado: "
                                    + registrationId
                    );
        };
    }
}