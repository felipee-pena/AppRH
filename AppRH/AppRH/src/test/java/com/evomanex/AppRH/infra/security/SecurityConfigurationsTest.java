package com.evomanex.AppRH.infra.security;

import com.evomanex.AppRH.infra.security.oauth.CustomOAuth2UserService;
import com.evomanex.AppRH.infra.security.oauth.CustomOidcUserService;
import com.evomanex.AppRH.infra.security.oauth.OAuthAuthenticationFailureHandler;
import com.evomanex.AppRH.infra.security.oauth.OAuthAuthenticationSuccessHandler;
import com.evomanex.AppRH.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SecurityTestController.class
)
@Import({
        SecurityConfigurations.class,
        SecurityFilter.class,
        SecurityConfigurationsTest.OAuthTestConfiguration.class
})
class SecurityConfigurationsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CustomOAuth2UserService
            customOAuth2UserService;

    @MockitoBean
    private CustomOidcUserService
            customOidcUserService;

    @MockitoBean
    private OAuthAuthenticationSuccessHandler
            oauthAuthenticationSuccessHandler;

    @MockitoBean
    private OAuthAuthenticationFailureHandler
            oauthAuthenticationFailureHandler;

    @Test
    void shouldAllowPublicRegistrationEndpoint()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/auth/register"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "login": "usuario@example.com",
                                          "password": "senha-segura"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string(
                                "registration"
                        )
                );
    }

    @Test
    void shouldAllowPublicEmailResendEndpoint()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/auth/email/resend"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "login": "usuario@example.com"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string(
                                "email-resend"
                        )
                );
    }

    @Test
    void shouldAllowPublicOAuthExchangeEndpoint()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/auth/oauth/exchange"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "code": "temporary-code"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string(
                                "oauth-exchange"
                        )
                );
    }

    @Test
    void shouldAllowPublicMfaVerificationEndpoint()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/auth/mfa/verify"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "challenge_token": "challenge",
                                          "code": "123456"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string(
                                "mfa-verification"
                        )
                );
    }

    @Test
    void shouldAllowPublicCompaniesEndpoint()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/api/companies"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string(
                                "companies"
                        )
                );
    }

    @Test
    void shouldRedirectGoogleOAuthAuthorizationEndpoint()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/oauth2/authorization/google"
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        header().exists(
                                "Location"
                        )
                )
                .andExpect(
                        redirectedUrlPattern(
                                "https://accounts.google.com/**"
                        )
                );
    }

    @Test
    void shouldRedirectGitHubOAuthAuthorizationEndpoint()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/oauth2/authorization/github"
                        )
                )
                .andExpect(
                        status().is3xxRedirection()
                )
                .andExpect(
                        header().exists(
                                "Location"
                        )
                )
                .andExpect(
                        redirectedUrlPattern(
                                "https://github.com/**"
                        )
                );
    }

    @Test
    void shouldRequireAuthenticationForJobsEndpoint()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/api/jobs"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldRequireAuthenticationForRhEndpoint()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/api/rh/company"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldRequireAuthenticationForAdministrativeEndpoint()
            throws Exception {
        mockMvc.perform(
                        get(
                                "/api/admin/users"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldRequireAuthenticationForMfaSetupEndpoint()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/api/account/mfa/totp/setup"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void shouldAllowCorsPreflightRequest()
            throws Exception {
        mockMvc.perform(
                        options(
                                "/api/jobs"
                        )
                                .header(
                                        "Origin",
                                        "http://localhost:5173"
                                )
                                .header(
                                        "Access-Control-Request-Method",
                                        "GET"
                                )
                                .header(
                                        "Access-Control-Request-Headers",
                                        "Authorization, Content-Type"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        header().string(
                                "Access-Control-Allow-Origin",
                                "http://localhost:5173"
                        )
                );
    }

    @Test
    void shouldRejectMalformedRegistrationJson()
            throws Exception {
        mockMvc.perform(
                        post(
                                "/auth/register"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "login":
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @TestConfiguration
    public static class OAuthTestConfiguration {

        @Bean
        public ClientRegistrationRepository
        clientRegistrationRepository() {
            ClientRegistration google =
                    createGoogleRegistration();

            ClientRegistration github =
                    createGitHubRegistration();

            return new InMemoryClientRegistrationRepository(
                    google,
                    github
            );
        }

        private ClientRegistration
        createGoogleRegistration() {
            return ClientRegistration
                    .withRegistrationId(
                            "google"
                    )
                    .clientId(
                            "google-test-client"
                    )
                    .clientSecret(
                            "google-test-secret"
                    )
                    .clientAuthenticationMethod(
                            ClientAuthenticationMethod
                                    .CLIENT_SECRET_BASIC
                    )
                    .authorizationGrantType(
                            AuthorizationGrantType
                                    .AUTHORIZATION_CODE
                    )
                    .redirectUri(
                            "{baseUrl}/login/oauth2/code/{registrationId}"
                    )
                    .scope(
                            "openid",
                            "profile",
                            "email"
                    )
                    .authorizationUri(
                            "https://accounts.google.com/o/oauth2/v2/auth"
                    )
                    .tokenUri(
                            "https://oauth2.googleapis.com/token"
                    )
                    .userInfoUri(
                            "https://openidconnect.googleapis.com/v1/userinfo"
                    )
                    .userNameAttributeName(
                            "sub"
                    )
                    .jwkSetUri(
                            "https://www.googleapis.com/oauth2/v3/certs"
                    )
                    .clientName(
                            "Google Test"
                    )
                    .build();
        }

        private ClientRegistration
        createGitHubRegistration() {
            return ClientRegistration
                    .withRegistrationId(
                            "github"
                    )
                    .clientId(
                            "github-test-client"
                    )
                    .clientSecret(
                            "github-test-secret"
                    )
                    .clientAuthenticationMethod(
                            ClientAuthenticationMethod
                                    .CLIENT_SECRET_BASIC
                    )
                    .authorizationGrantType(
                            AuthorizationGrantType
                                    .AUTHORIZATION_CODE
                    )
                    .redirectUri(
                            "{baseUrl}/login/oauth2/code/{registrationId}"
                    )
                    .scope(
                            "read:user",
                            "user:email"
                    )
                    .authorizationUri(
                            "https://github.com/login/oauth/authorize"
                    )
                    .tokenUri(
                            "https://github.com/login/oauth/access_token"
                    )
                    .userInfoUri(
                            "https://api.github.com/user"
                    )
                    .userNameAttributeName(
                            "id"
                    )
                    .clientName(
                            "GitHub Test"
                    )
                    .build();
        }
    }
}