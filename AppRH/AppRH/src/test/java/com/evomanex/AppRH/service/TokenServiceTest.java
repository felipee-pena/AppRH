package com.evomanex.AppRH.service;

import com.auth0.jwt.JWT;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import com.evomanex.AppRH.infra.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private static final String SECRET =
            "unit-test-secret-with-at-least-32-characters";

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService =
                new TokenService();

        ReflectionTestUtils.setField(
                tokenService,
                "secret",
                SECRET
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        String token =
                tokenService.generateToken(
                        user
                );

        String subject =
                tokenService.validateToken(
                        token
                );

        assertThat(
                token
        ).isNotBlank();

        assertThat(
                subject
        ).isEqualTo(
                "usuario@example.com"
        );
    }

    @Test
    void shouldIncludeRoleClaim() {
        User user =
                new User(
                        "rh@example.com",
                        "$2a$10$encoded",
                        UserRole.USERRH
                );

        String token =
                tokenService.generateToken(
                        user
                );

        String role =
                JWT.decode(
                                token
                        )
                        .getClaim(
                                "role"
                        )
                        .asString();

        assertThat(
                role
        ).isEqualTo(
                "USERRH"
        );
    }

    @Test
    void shouldRejectTamperedToken() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        String token =
                tokenService.generateToken(
                        user
                );

        String tamperedToken =
                token.substring(
                        0,
                        token.length() - 1
                )
                        + (
                        token.endsWith("a")
                                ? "b"
                                : "a"
                );

        assertThat(
                tokenService.validateToken(
                        tamperedToken
                )
        ).isEmpty();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        TokenService anotherTokenService =
                new TokenService();

        ReflectionTestUtils.setField(
                anotherTokenService,
                "secret",
                "another-secret-with-at-least-32-characters"
        );

        String token =
                anotherTokenService.generateToken(
                        user
                );

        assertThat(
                tokenService.validateToken(
                        token
                )
        ).isEmpty();
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThat(
                tokenService.validateToken(
                        "token-invalido"
                )
        ).isEmpty();
    }
}