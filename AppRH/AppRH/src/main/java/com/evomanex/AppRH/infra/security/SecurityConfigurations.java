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
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {

        return httpSecurity

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize ->
                        authorize

                                /*
                                 * Login e cadastro público.
                                 *
                                 * O cadastro público permite somente
                                 * os perfis USER e USERRH.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/auth/login",
                                        "/auth/register"
                                ).permitAll()

                                /*
                                 * Swagger e OpenAPI.
                                 */
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs.yaml"
                                ).permitAll()

                                /*
                                 * Endpoint interno de erros.
                                 */
                                .requestMatchers(
                                        "/error"
                                ).permitAll()

                                /*
                                 * Consulta pública das empresas.
                                 *
                                 * Necessária para que a página pública
                                 * de cadastro carregue as empresas antes
                                 * de o usuário possuir um token.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/companies",
                                        "/api/companies/**"
                                ).permitAll()

                                /*
                                 * Consulta e vínculo da empresa
                                 * do usuário de RH autenticado.
                                 */
                                .requestMatchers(
                                        "/api/rh/company",
                                        "/api/rh/company/**"
                                ).hasRole("USERRH")

                                /*
                                 * Catálogo de vagas disponível para
                                 * qualquer usuário autenticado.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/jobs",
                                        "/api/jobs/**"
                                ).authenticated()

                                /*
                                 * Gestão de vagas disponível para
                                 * USERRH e ADMIN.
                                 *
                                 * ADMIN também possui ROLE_USERRH
                                 * nas authorities do modelo atual.
                                 */
                                .requestMatchers(
                                        "/api/management/jobs",
                                        "/api/management/jobs/**"
                                ).hasRole("USERRH")

                                /*
                                 * Endpoints administrativos.
                                 */
                                .requestMatchers(
                                        "/api/admin",
                                        "/api/admin/**"
                                ).hasRole("ADMIN")

                                /*
                                 * Esta deve ser sempre a última regra.
                                 */
                                .anyRequest()
                                .authenticated()
                )

                .exceptionHandling(exceptionHandling ->
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

        return configuration.getAuthenticationManager();
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