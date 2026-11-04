package com.evomanex.AppRH.infra.security.oauth;

public record OAuthProviderProfile(
        ExternalIdentityProvider provider,
        String providerUserId,
        String email,
        boolean emailVerified,
        String username
) {
}