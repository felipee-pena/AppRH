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

                /*
                 * Configura o CORS entre frontend e backend.
                 */
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                /*
                 * A API utiliza JWT e não depende
                 * de sessão HTTP ou formulário.
                 */
                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Regras de autenticação e autorização.
                 *
                 * A ordem vai das rotas mais específicas
                 * para a regra genérica final.
                 */
                .authorizeHttpRequests(authorize ->
                        authorize

                                /*
                                 * Endpoints públicos de autenticação.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/auth/login",
                                        "/auth/register"
                                ).permitAll()

                                /*
                                 * Recursos necessários para carregar
                                 * o Swagger e o contrato OpenAPI.
                                 */
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs.yaml"
                                ).permitAll()

                                /*
                                 * Permite que erros sejam processados
                                 * pelo Spring sem bloqueio adicional.
                                 */
                                .requestMatchers(
                                        "/error"
                                ).permitAll()

                                /*
                                 * Empresa associada ao perfil RH.
                                 *
                                 * A URL exige ROLE_USERRH.
                                 * O service ainda verifica se a role
                                 * efetiva é exatamente USERRH.
                                 */
                                .requestMatchers(
                                        "/api/rh/company",
                                        "/api/rh/company/**"
                                ).hasRole("USERRH")

                                /*
                                 * Catálogo de empresas disponível
                                 * para qualquer usuário autenticado.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/companies",
                                        "/api/companies/**"
                                ).authenticated()

                                /*
                                 * Catálogo de vagas disponível
                                 * para qualquer usuário autenticado.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/jobs",
                                        "/api/jobs/**"
                                ).authenticated()

                                /*
                                 * Gestão de vagas disponível para
                                 * ADMIN e USERRH.
                                 *
                                 * O ADMIN possui ROLE_USERRH na
                                 * hierarquia atual das authorities.
                                 */
                                .requestMatchers(
                                        "/api/management/jobs",
                                        "/api/management/jobs/**"
                                ).hasRole("USERRH")

                                /*
                                 * Recursos administrativos exclusivos
                                 * do perfil ADMIN.
                                 */
                                .requestMatchers(
                                        "/api/admin",
                                        "/api/admin/**"
                                ).hasRole("ADMIN")

                                /*
                                 * Qualquer outra rota exige uma
                                 * autenticação válida.
                                 */
                                .anyRequest()
                                .authenticated()
                )

                /*
                 * Respostas padronizadas dos erros
                 * produzidos na cadeia de segurança.
                 */
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling

                                /*
                                 * Retornado quando não existe
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
                                                              "message": "Autenticação necessária",
                                                              "errors": {}
                                                            }
                                                            """
                                                    );
                                        }
                                )

                                /*
                                 * Retornado quando o usuário está
                                 * autenticado, mas não possui a
                                 * autoridade exigida pela URL.
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
                                                              "message": "Acesso negado",
                                                              "errors": {}
                                                            }
                                                            """
                                                    );
                                        }
                                )
                )

                /*
                 * Executa a validação JWT antes do filtro
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

        /*
         * Frontend Vite durante o desenvolvimento.
         */
        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        /*
         * PATCH já fica liberado para futuras
         * atualizações parciais de usuários,
         * empresas e outros recursos.
         */
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

        /*
         * O JWT é enviado pelo header Authorization.
         * A aplicação não utiliza cookie JSESSIONID.
         */
        configuration.setAllowCredentials(
                false
        );

        /*
         * Mantém a resposta do preflight CORS em
         * cache por uma hora.
         */
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