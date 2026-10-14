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

                /*
                 * Configura o CORS entre frontend e backend.
                 */
                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                /*
                 * A API usa JWT e não depende de sessão ou formulário.
                 */
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Regras de autenticação e autorização.
                 */
                .authorizeHttpRequests(authorize -> authorize

                        /*
                         * Login e cadastro ficam públicos.
                         *
                         * O cadastro público deve criar
                         * exclusivamente usuários USER.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/auth/register"
                        ).permitAll()

                        /*
                         * Swagger e OpenAPI ficam disponíveis
                         * somente para ADMIN.
                         */
                                .requestMatchers(
                                        "/swagg***ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/v3/ap***ocs.yaml"
                         ).permitAll()

                        /*
                         * Endpoint interno de tratamento de erros.
                         */
                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        /*
                         * Catálogo de vagas.
                         *
                         * Todos os usuários autenticados podem
                         * consultar as vagas.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/jobs",
                                "/api/jobs/**"
                        ).authenticated()

                        /*
                         * Gestão de vagas.
                         *
                         * USERRH pode gerenciar suas vagas.
                         * ADMIN também acessa porque possui
                         * ROLE_USERRH no getAuthorities().
                         */
                        .requestMatchers(
                                "/api/management/jobs",
                                "/api/management/jobs/**"
                        ).hasRole("USERRH")

                        /*
                         * Endpoints exclusivamente administrativos.
                         */
                        .requestMatchers(
                                "/api/admin",
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        /*
                         * Qualquer outro endpoint exige autenticação.
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * Respostas padronizadas para erros de segurança.
                 */
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling

                                /*
                                 * Retornado quando não existe uma
                                 * autenticação válida.
                                 */
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
                                                              "message": "Autenticação necessária"
                                                            }
                                                            """
                                                    );
                                        }
                                )

                                /*
                                 * Retornado quando o usuário está
                                 * autenticado, mas não possui a role.
                                 */
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
                                                              "message": "Acesso negado"
                                                            }
                                                            """
                                                    );
                                        }
                                )
                )

                /*
                 * Executa o filtro JWT antes do filtro
                 * padrão de autenticação do Spring.
                 */
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

        /*
         * Frontend Vite durante o desenvolvimento.
         */
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
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
                List.of("Authorization")
        );

        /*
         * O JWT é enviado pelo header Authorization.
         * A aplicação não usa cookie JSESSIONID.
         */
        configuration.setAllowCredentials(false);

        /*
         * Cache da resposta de preflight CORS.
         */
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}