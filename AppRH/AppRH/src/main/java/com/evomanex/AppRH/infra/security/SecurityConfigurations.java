package com.evomanex.AppRH.infra.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    public SecurityConfigurations(
            SecurityFilter securityFilter
    ) {
        this.securityFilter =
                securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {
        return httpSecurity

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
                                         * Cadastro, login e confirmação
                                         * de e-mail são públicos.
                                         */
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/auth/login",
                                                "/auth/register",
                                                "/auth/email/confirm",
                                                "/auth/email/resend"
                                        )
                                        .permitAll()

                                        /*
                                         * Swagger UI e contrato OpenAPI.
                                         */
                                        .requestMatchers(
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs",
                                                "/v3/api-docs/**",
                                                "/v3/api-docs.yaml",
                                                "/webjars/**"
                                        )
                                        .permitAll()

                                        /*
                                         * Processamento interno de erros.
                                         */
                                        .requestMatchers(
                                                "/error"
                                        )
                                        .permitAll()

                                        /*
                                         * Lista pública de empresas,
                                         * necessária para cadastro de RH.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/companies",
                                                "/api/companies/**"
                                        )
                                        .permitAll()

                                        /*
                                         * Associação da empresa ao RH.
                                         */
                                        .requestMatchers(
                                                "/api/rh/company",
                                                "/api/rh/company/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        /*
                                         * Catálogo de vagas.
                                         */
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/jobs",
                                                "/api/jobs/**"
                                        )
                                        .authenticated()

                                        /*
                                         * Gestão de vagas.
                                         */
                                        .requestMatchers(
                                                "/api/management/jobs",
                                                "/api/management/jobs/**"
                                        )
                                        .hasRole(
                                                "USERRH"
                                        )

                                        /*
                                         * Área administrativa.
                                         */
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
                                                        authenticationException
                                                ) -> {
                                                    response.setStatus(
                                                            HttpServletResponse
                                                                    .SC_UNAUTHORIZED
                                                    );

                                                    response.setContentType(
                                                            "application/json"
                                                    );

                                                    response.setCharacterEncoding(
                                                            "UTF-8"
                                                    );

                                                    response
                                                            .getWriter()
                                                            .write(
                                                                    """
                                                                    {
                                                                      "status": 401,
                                                                      "message": "Autenticação necessária",
                                                                      "errors": {}
                                                                    }
                                                                    """
                                                            );
                                                }
                                        )

                                        .accessDeniedHandler(
                                                (
                                                        request,
                                                        response,
                                                        accessDeniedException
                                                ) -> {
                                                    response.setStatus(
                                                            HttpServletResponse
                                                                    .SC_FORBIDDEN
                                                    );

                                                    response.setContentType(
                                                            "application/json"
                                                    );

                                                    response.setCharacterEncoding(
                                                            "UTF-8"
                                                    );

                                                    response
                                                            .getWriter()
                                                            .write(
                                                                    """
                                                                    {
                                                                      "status": 403,
                                                                      "message": "Acesso negado",
                                                                      "errors": {}
                                                                    }
                                                                    """
                                                            );
                                                }
                                        )
                )

                .addFilterBefore(
                        securityFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
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
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        configuration.setAllowCredentials(
                false
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