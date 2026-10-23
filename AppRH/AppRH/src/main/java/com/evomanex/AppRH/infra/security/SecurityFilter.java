package com.evomanex.AppRH.infra.security;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        String path =
                request.getServletPath();

        String method =
                request.getMethod();

        if (
                HttpMethod.OPTIONS.matches(
                        method
                )
        ) {
            return true;
        }

        if (
                HttpMethod.POST.matches(
                        method
                )
        ) {
            return path.equals(
                    "/auth/login"
            )
                    || path.equals(
                    "/auth/register"
            )
                    || path.equals(
                    "/auth/email/confirm"
            )
                    || path.equals(
                    "/auth/email/resend"
            );
        }

        if (
                HttpMethod.GET.matches(
                        method
                )
        ) {
            return path.equals(
                    "/api/companies"
            )
                    || path.startsWith(
                    "/api/companies/"
            )
                    || path.equals(
                    "/v3/api-docs"
            )
                    || path.startsWith(
                    "/v3/api-docs/"
            )
                    || path.equals(
                    "/v3/api-docs.yaml"
            )
                    || path.equals(
                    "/swagger-ui.html"
            )
                    || path.startsWith(
                    "/swagger-ui/"
            )
                    || path.startsWith(
                    "/webjars/"
            );
        }

        return path.equals(
                "/error"
        );
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
            UserDetails userDetails =
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