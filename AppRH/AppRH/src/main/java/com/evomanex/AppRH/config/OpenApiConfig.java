package com.evomanex.AppRH.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME =
            "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {

        SecurityScheme securityScheme =
                new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                                "Informe somente o access_token JWT. "
                                        + "Não inclua a palavra Bearer."
                        );

        SecurityRequirement securityRequirement =
                new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME);

        Components components =
                new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                securityScheme
                        );

        Info info =
                new Info()
                        .title("AppRH API")
                        .description(
                                "API REST para autenticação, "
                                        + "gerenciamento de vagas "
                                        + "e candidaturas."
                        )
                        .version("1.0.0")
                        .contact(
                                new Contact()
                                        .name("AppRH")
                        )
                        .license(
                                new License()
                                        .name(
                                                "Uso para o Projeto "
                                                        + "AppRH Evomanex"
                                        )
                        );

        return new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}