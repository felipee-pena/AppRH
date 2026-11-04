package com.evomanex.AppRH.domain.oauth;

import com.evomanex.AppRH.infra.security.oauth.OAuthAuthenticationFailureHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OAuthAuthenticationFailureHandlerTest {

    private static final String FRONTEND_URL =
            "http://localhost:5173";

    private OAuthAuthenticationFailureHandler handler;

    @BeforeEach
    void setUp() {
        handler =
                new OAuthAuthenticationFailureHandler(
                        FRONTEND_URL
                );
    }

    @Test
    void shouldRedirectToFrontendLoginPage()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AuthenticationException exception =
                mock(
                        AuthenticationException.class
                );

        handler.onAuthenticationFailure(
                request,
                response,
                exception
        );

        assertThat(
                response.getStatus()
        ).isEqualTo(
                302
        );

        assertThat(
                response.getRedirectedUrl()
        ).isNotNull();

        assertThat(
                response.getRedirectedUrl()
        ).startsWith(
                FRONTEND_URL + "/login"
        );
    }

    @Test
    void shouldAddOAuthErrorQueryParameter()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AuthenticationException exception =
                mock(
                        AuthenticationException.class
                );

        handler.onAuthenticationFailure(
                request,
                response,
                exception
        );

        String redirectedUrl =
                response.getRedirectedUrl();

        assertThat(
                redirectedUrl
        ).isNotNull();

        assertThat(
                redirectedUrl
        ).contains(
                "oauthError="
        );

        String decodedUrl =
                URLDecoder.decode(
                        redirectedUrl,
                        StandardCharsets.UTF_8
                );

        assertThat(
                decodedUrl
        ).contains(
                "Não foi possível autenticar "
                        + "com o provedor selecionado."
        );
    }

    @Test
    void shouldEncodeOAuthError()
            throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AuthenticationException exception =
                mock(
                        AuthenticationException.class
                );

        handler.onAuthenticationFailure(
                request,
                response,
                exception
        );

        String redirectedUrl =
                response.getRedirectedUrl();

        assertThat(
                redirectedUrl
        ).isNotNull();

        assertThat(
                redirectedUrl
        ).doesNotContain(
                " "
        );

        assertThat(
                redirectedUrl
        ).contains(
                "%C3%A3"
        );
    }

    @Test
    void shouldUseConfiguredFrontendUrl()
            throws Exception {
        OAuthAuthenticationFailureHandler customHandler =
                new OAuthAuthenticationFailureHandler(
                        "https://frontend.example.com"
                );

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        AuthenticationException exception =
                mock(
                        AuthenticationException.class
                );

        customHandler.onAuthenticationFailure(
                request,
                response,
                exception
        );

        URI redirectUri =
                URI.create(
                        response.getRedirectedUrl()
                );

        assertThat(
                redirectUri.getScheme()
        ).isEqualTo(
                "https"
        );

        assertThat(
                redirectUri.getHost()
        ).isEqualTo(
                "frontend.example.com"
        );

        assertThat(
                redirectUri.getPath()
        ).isEqualTo(
                "/login"
        );

        assertThat(
                redirectUri.getRawQuery()
        ).contains(
                "oauthError="
        );
    }
}