package com.evomanex.AppRH.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI appRhOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("AppRH API")
                                .description(
                                        "API REST para gerenciamento de vagas e candidatos"
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("AppRH")
                                )
                                .license(
                                        new License()
                                                .name("Uso para o Projeto AppRH Evomanex")
                                )
                );
    }
}