package com.evomanex.AppRH.infra.security;

import com.evomanex.AppRH.domain.user.User;

import com.evomanex.AppRH.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter
        extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final UserRepository userRepository;

    public SecurityFilter(
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.tokenService =
                tokenService;

        this.userRepository =
                userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token =
                recoverToken(
                        request
                );

        /*
         * Ausência do JWT não é erro do filtro.
         * A autorização da rota será decidida pela
         * SecurityFilterChain.
         */
        if (
                token == null
                        || token.isBlank()
        ) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String login =
                tokenService.validateToken(
                        token
                );

        if (
                login != null
                        && !login.isBlank()
                        && SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        == null
        ) {
            var userDetails =
                    userRepository.findByLogin(
                            login
                    );

            if (userDetails != null) {
                User user =
                        (User) userDetails;

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities()
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private String recoverToken(
            HttpServletRequest request
    ) {
        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );

        if (
                authorizationHeader == null
                        || !authorizationHeader.startsWith(
                        "Bearer "
                )
        ) {
            return null;
        }

        return authorizationHeader
                .substring(7)
                .trim();
    }
}