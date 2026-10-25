package com.evomanex.AppRH.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config =
            new OpenApiConfig();

    @Test
    void shouldConfigureApiInformation() {
        OpenAPI openAPI =
                config.customOpenAPI();

        Info info =
                openAPI.getInfo();

        assertThat(
                info
        ).isNotNull();

        assertThat(
                info.getTitle()
        ).isEqualTo(
                "AppRH API"
        );

        assertThat(
                info.getDescription()
        ).isEqualTo(
                "API REST para autenticação, "
                        + "gerenciamento de vagas "
                        + "e candidaturas."
        );

        assertThat(
                info.getVersion()
        ).isEqualTo(
                "1.0.0"
        );

        assertThat(
                info.getContact()
        ).isNotNull();

        assertThat(
                info.getContact().getName()
        ).isEqualTo(
                "AppRH"
        );

        assertThat(
                info.getLicense()
        ).isNotNull();

        assertThat(
                info.getLicense().getName()
        ).isEqualTo(
                "Uso para o Projeto AppRH Evomanex"
        );
    }

    @Test
    void shouldConfigureBearerJwtSecurityScheme() {
        OpenAPI openAPI =
                config.customOpenAPI();

        SecurityScheme securityScheme =
                openAPI
                        .getComponents()
                        .getSecuritySchemes()
                        .get(
                                OpenApiConfig.SECURITY_SCHEME_NAME
                        );

        assertThat(
                securityScheme
        ).isNotNull();

        assertThat(
                securityScheme.getName()
        ).isEqualTo(
                "bearerAuth"
        );

        assertThat(
                securityScheme.getType()
        ).isEqualTo(
                SecurityScheme.Type.HTTP
        );

        assertThat(
                securityScheme.getScheme()
        ).isEqualTo(
                "bearer"
        );

        assertThat(
                securityScheme.getBearerFormat()
        ).isEqualTo(
                "JWT"
        );

        assertThat(
                securityScheme.getDescription()
        ).contains(
                "access_token JWT"
        );
    }

    @Test
    void shouldApplySecurityRequirement() {
        OpenAPI openAPI =
                config.customOpenAPI();

        assertThat(
                openAPI.getSecurity()
        ).hasSize(
                1
        );

        assertThat(
                openAPI
                        .getSecurity()
                        .getFirst()
        ).containsKey(
                OpenApiConfig.SECURITY_SCHEME_NAME
        );
    }
}