package com.evomanex.AppRH.infra.security;

import com.evomanex.AppRH.infra.security.oauth.CustomOAuth2UserService;
import com.evomanex.AppRH.infra.security.oauth.CustomOidcUserService;
import com.evomanex.AppRH.infra.security.oauth.OAuthAuthenticationFailureHandler;
import com.evomanex.AppRH.infra.security.oauth.OAuthAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;

    private final CustomOAuth2UserService customOAuth2UserService;

    private final CustomOidcUserService customOidcUserService;

    private final OAuthAuthenticationSuccessHandler oauthSuccessHandler;

    private final OAuthAuthenticationFailureHandler oauthFailureHandler;

    public SecurityConfigurations(
            SecurityFilter securityFilter,
            CustomOAuth2UserService customOAuth2UserService,
            CustomOidcUserService customOidcUserService,
            OAuthAuthenticationSuccessHandler oauthSuccessHandler,
            OAuthAuthenticationFailureHandler oauthFailureHandler
    ) {
        this.securityFilter = securityFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oauthSuccessHandler = oauthSuccessHandler;
        this.oauthFailureHandler = oauthFailureHandler;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {
        return httpSecurity
                .securityMatcher(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/actuator/info",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error",
                        "/auth/login",
                        "/auth/register",
                        "/auth/email/confirm",
                        "/auth/email/resend",
                        "/auth/oauth/exchange",
                        "/auth/mfa/verify",
                        "/api/companies",
                        "/api/companies/**"
                )
                .cors(
                        cors ->
                                cors.configurationSource(
                                        corsConfigurationSource()
                                )
                )
                .csrf(
                        csrf ->
                                csrf.ignoringRequestMatchers(
                                        "/auth/login",
                                        "/auth/register",
                                        "/auth/email/confirm",
                                        "/auth/email/resend",
                                        "/auth/oauth/exchange",
                                        "/auth/mfa/verify",
                                        "/api/companies",
                                        "/api/companies/**"
                                )
                )
                .httpBasic(
                        httpBasic ->
                                httpBasic.disable()
                )
                .formLogin(
                        formLogin ->
                                formLogin.disable()
                )
                .requestCache(
                        requestCache ->
                                requestCache.disable()
                )
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        /*
                                         * Requisições de preflight do navegador.
                                         */
                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()

                                        /*
                                         * Actuator público.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/actuator/health",
                                                "/actuator/health/**",
                                                "/actuator/info"
                                        )
                                        .permitAll()

                                        /*
                                         * Swagger e OpenAPI.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs",
                                                "/v3/api-docs/**",
                                                "/v3/api-docs.yaml",
                                                "/webjars/**",
                                                "/favicon.ico"
                                        )
                                        .permitAll()

                                        /*
                                         * Login tradicional com usuário e senha.
                                         */
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/auth/login"
                                        )
                                        .permitAll()

                                        /*
                                         * Cadastro de usuário.
                                         */
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/auth/register"
                                        )
                                        .permitAll()

                                        /*
                                         * Confirmação do e-mail pelo link enviado
                                         * ao usuário.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/auth/email/confirm"
                                        )
                                        .permitAll()

                                        /*
                                         * Reenvio do e-mail de confirmação.
                                         */
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/auth/email/resend"
                                        )
                                        .permitAll()

                                        /*
                                         * Troca do código temporário OAuth
                                         * pelo token da aplicação.
                                         */
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/auth/oauth/exchange"
                                        )
                                        .permitAll()

                                        /*
                                         * Verificação do código MFA durante
                                         * o processo de login.
                                         */
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/auth/mfa/verify"
                                        )
                                        .permitAll()

                                        /*
                                         * Consulta pública das empresas.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/companies",
                                                "/api/companies/**"
                                        )
                                        .permitAll()

                                        /*
                                         * O Spring Boot pode encaminhar erros
                                         * internos para /error utilizando o
                                         * mesmo método da requisição original.
                                         */
                                        .requestMatchers(
                                                "/error"
                                        )
                                        .permitAll()

                                        /*
                                         * Mantemos os caminhos desta cadeia
                                         * acessíveis ao DispatcherServlet para
                                         * que métodos incorretos resultem em 405,
                                         * conforme o mapeamento dos controllers.
                                         */
                                        .anyRequest()
                                        .permitAll()
                )
                .exceptionHandling(
                        exceptionHandling ->
                                exceptionHandling
                                        .authenticationEntryPoint(
                                                (
                                                        request,
                                                        response,
                                                        exception
                                                ) ->
                                                        writeUnauthorizedResponse(
                                                                response
                                                        )
                                        )
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain applicationSecurityFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {
        return httpSecurity
                .securityMatcher(
                        "/**"
                )
                .cors(
                        cors ->
                                cors.configurationSource(
                                        corsConfigurationSource()
                                )
                )
                .csrf(
                        csrf ->
                                csrf.ignoringRequestMatchers(
                                        "/api/**"
                                )
                )
                .httpBasic(
                        httpBasic ->
                                httpBasic.disable()
                )
                .formLogin(
                        formLogin ->
                                formLogin.disable()
                )
                .requestCache(
                        requestCache ->
                                requestCache.disable()
                )
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.IF_REQUIRED
                                )
                )
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        /*
                                         * Preflight CORS.
                                         */
                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()

                                        /*
                                         * Início do login OAuth2/OIDC.
                                         *
                                         * Exemplos:
                                         * GET /oauth2/authorization/google
                                         * GET /oauth2/authorization/github
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/oauth2/**"
                                        )
                                        .permitAll()

                                        /*
                                         * Callback recebido do Google ou GitHub.
                                         *
                                         * Exemplos:
                                         * GET /login/oauth2/code/google
                                         * GET /login/oauth2/code/github
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/login/oauth2/**"
                                        )
                                        .permitAll()

                                        /*
                                         * Configuração e administração do MFA
                                         * da conta autenticada.
                                         */
                                        .requestMatchers(
                                                "/api/account/mfa/**"
                                        )
                                        .authenticated()

                                        /*
                                         * Consulta da empresa vinculada ao RH.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/rh/company",
                                                "/api/rh/company/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        /*
                                         * Cadastro ou atualização da empresa
                                         * vinculada ao RH.
                                         */
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/rh/company",
                                                "/api/rh/company/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                HttpMethod.PUT,
                                                "/api/rh/company",
                                                "/api/rh/company/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                HttpMethod.PATCH,
                                                "/api/rh/company",
                                                "/api/rh/company/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        /*
                                         * Consulta das vagas.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/jobs",
                                                "/api/jobs/**"
                                        )
                                        .authenticated()

                                        /*
                                         * Gerenciamento das vagas pelo RH.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/management/jobs",
                                                "/api/management/jobs/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/management/jobs",
                                                "/api/management/jobs/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                HttpMethod.PUT,
                                                "/api/management/jobs",
                                                "/api/management/jobs/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                HttpMethod.PATCH,
                                                "/api/management/jobs",
                                                "/api/management/jobs/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                HttpMethod.DELETE,
                                                "/api/management/jobs",
                                                "/api/management/jobs/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        /*
                                         * Endpoints administrativos.
                                         */
                                        .requestMatchers(
                                                "/api/admin",
                                                "/api/admin/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

                                        /*
                                         * Qualquer outro endpoint exige
                                         * usuário autenticado.
                                         */
                                        .anyRequest()
                                        .authenticated()
                )
                .exceptionHandling(
                        exceptionHandling ->
                                exceptionHandling
                                        .authenticationEntryPoint(
                                                (
                                                        request,
                                                        response,
                                                        exception
                                                ) ->
                                                        writeUnauthorizedResponse(
                                                                response
                                                        )
                                        )
                                        .accessDeniedHandler(
                                                (
                                                        request,
                                                        response,
                                                        exception
                                                ) ->
                                                        writeForbiddenResponse(
                                                                response
                                                        )
                                        )
                )
                .oauth2Login(
                        oauth2 ->
                                oauth2
                                        .userInfoEndpoint(
                                                userInfo ->
                                                        userInfo
                                                                .userService(
                                                                        customOAuth2UserService
                                                                )
                                                                .oidcUserService(
                                                                        customOidcUserService
                                                                )
                                        )
                                        .successHandler(
                                                oauthSuccessHandler
                                        )
                                        .failureHandler(
                                                oauthFailureHandler
                                        )
                )
                .addFilterBefore(
                        securityFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    private void writeUnauthorizedResponse(
            HttpServletResponse response
    ) throws java.io.IOException {
        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
                """
                {
                  "status": 401,
                  "message": "Autenticação necessária",
                  "errors": {}
                }
                """
        );
    }

    private void writeForbiddenResponse(
            HttpServletResponse response
    ) throws java.io.IOException {
        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
                """
                {
                  "status": 403,
                  "message": "Acesso negado",
                  "errors": {}
                }
                """
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Origin"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        configuration.setAllowCredentials(
                true
        );

        configuration.setMaxAge(
                3600L
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}