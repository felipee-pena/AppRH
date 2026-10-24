package com.evomanex.AppRH.infra.security;

import com.evomanex.AppRH.controllers.AuthenticationController;
import com.evomanex.AppRH.repository.UserRepository;
import com.evomanex.AppRH.service.EmailVerificationService;
import com.evomanex.AppRH.service.PublicRegistrationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthenticationController.class
)
@Import({
        SecurityConfigurations.class,
        SecurityFilter.class
})
class SecurityConfigurationsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private PublicRegistrationService publicRegistrationService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @Test
    void shouldAllowPublicEmailResendEndpoint() throws Exception {
        mockMvc.perform(
                        post("/auth/email/resend")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "login": "usuario@example.com"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Se a conta existir e ainda estiver pendente, "
                                                + "um novo e-mail de confirmação será enviado"
                                )
                );

        verify(emailVerificationService)
                .resendVerification("usuario@example.com");
    }

    @Test
    void shouldAllowPublicRegistrationEndpoint() throws Exception {

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "login": "",
                                          "password": "",
                                          "role": null,
                                          "companyId": null,
                                          "newCompanyName": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Dados inválidos")
                );

        /*
         * O 400 comprova que a requisição chegou ao controller
         * e foi processada pela validação.
         *
         * Se a rota não fosse pública, retornaria 401.
         */
    }

    @Test
    void shouldRequireAuthenticationForJobsEndpoint() throws Exception {

        mockMvc.perform(
                        get("/api/jobs")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Autenticação necessária")
                );
    }

    @Test
    void shouldRequireAuthenticationForAdministrativeEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/admin/users")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Autenticação necessária")
                );
    }

    @Test
    void shouldRequireAuthenticationForRhEndpoint()
            throws Exception {

        mockMvc.perform(
                        get("/api/rh/company")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                );
    }

    @Test
    void shouldIgnoreOptionsRequests()
            throws Exception {

        mockMvc.perform(
                        options("/api/jobs")
                                .header(
                                        "Origin",
                                        "http://localhost:5173"
                                )
                                .header(
                                        "Access-Control-Request-Method",
                                        "GET"
                                )
                )
                .andExpect(result -> {

                    int responseStatus =
                            result.getResponse()
                                    .getStatus();

                    assertThat(responseStatus)
                            .isNotEqualTo(401);
                });
    }

    @Test
    void shouldRejectMalformedRegistrationJson()
            throws Exception {

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "login": "usuario@example.com",
                                          "password":
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }
}