package com.evomanex.AppRH.infra.security.oauth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalIdentityProviderTest {

    @Test
    void shouldResolveGoogleProvider() {
        ExternalIdentityProvider provider =
                ExternalIdentityProvider
                        .fromRegistrationId(
                                "google"
                        );

        assertThat(
                provider
        ).isEqualTo(
                ExternalIdentityProvider.GOOGLE
        );
    }

    @Test
    void shouldResolveGitHubProvider() {
        ExternalIdentityProvider provider =
                ExternalIdentityProvider
                        .fromRegistrationId(
                                "github"
                        );

        assertThat(
                provider
        ).isEqualTo(
                ExternalIdentityProvider.GITHUB
        );
    }

    @Test
    void shouldIgnoreCase() {
        assertThat(
                ExternalIdentityProvider
                        .fromRegistrationId(
                                "GOOGLE"
                        )
        ).isEqualTo(
                ExternalIdentityProvider.GOOGLE
        );

        assertThat(
                ExternalIdentityProvider
                        .fromRegistrationId(
                                "GitHub"
                        )
        ).isEqualTo(
                ExternalIdentityProvider.GITHUB
        );
    }

    @Test
    void shouldTrimRegistrationId() {
        assertThat(
                ExternalIdentityProvider
                        .fromRegistrationId(
                                "  google  "
                        )
        ).isEqualTo(
                ExternalIdentityProvider.GOOGLE
        );
    }

    @Test
    void shouldRejectNullRegistrationId() {
        assertThatThrownBy(
                () ->
                        ExternalIdentityProvider
                                .fromRegistrationId(
                                        null
                                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O identificador do provedor "
                                + "é obrigatório."
                );
    }

    @Test
    void shouldRejectBlankRegistrationId() {
        assertThatThrownBy(
                () ->
                        ExternalIdentityProvider
                                .fromRegistrationId(
                                        " "
                                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O identificador do provedor "
                                + "é obrigatório."
                );
    }

    @Test
    void shouldRejectUnsupportedProvider() {
        assertThatThrownBy(
                () ->
                        ExternalIdentityProvider
                                .fromRegistrationId(
                                        "facebook"
                                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Provedor OAuth não suportado: facebook"
                );
    }
}