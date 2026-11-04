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
        this.securityFilter =
                securityFilter;

        this.customOAuth2UserService =
                customOAuth2UserService;

        this.customOidcUserService =
                customOidcUserService;

        this.oauthSuccessHandler =
                oauthSuccessHandler;

        this.oauthFailureHandler =
                oauthFailureHandler;
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
                                csrf.disable()
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
                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()
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
                                csrf.disable()
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
                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/oauth2/**",
                                                "/login/oauth2/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/api/account/mfa/**"
                                        )
                                        .authenticated()

                                        .requestMatchers(
                                                "/api/rh/company",
                                                "/api/rh/company/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/jobs",
                                                "/api/jobs/**"
                                        )
                                        .authenticated()

                                        .requestMatchers(
                                                "/api/management/jobs",
                                                "/api/management/jobs/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        .requestMatchers(
                                                "/api/admin",
                                                "/api/admin/**"
                                        )
                                        .hasRole(
                                                "ADMIN"
                                        )

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
    public CorsConfigurationSource
    corsConfigurationSource() {
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