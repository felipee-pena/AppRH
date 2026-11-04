package com.evomanex.AppRH.infra.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuthAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    OAuthAuthenticationFailureHandler.class
            );

    private final String frontendUrl;

    public OAuthAuthenticationFailureHandler(
            @Value(
                    "${app.frontend-url:http://localhost:5173}"
            )
            String frontendUrl
    ) {
        this.frontendUrl =
                frontendUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        LOGGER.error(
                "Falha na autenticação OAuth. "
                        + "Tipo: {}. Mensagem: {}",
                exception.getClass().getName(),
                exception.getMessage(),
                exception
        );

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(
                                frontendUrl
                        )
                        .path(
                                "/login"
                        )
                        .queryParam(
                                "oauthError",
                                "Não foi possível autenticar "
                                        + "com o provedor selecionado."
                        )
                        .build()
                        .encode()
                        .toUriString();

        response.sendRedirect(
                redirectUrl
        );
    }
}