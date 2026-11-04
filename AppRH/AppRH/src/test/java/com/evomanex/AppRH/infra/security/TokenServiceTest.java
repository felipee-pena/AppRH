package com.evomanex.AppRH.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private static final String SECRET =
            "test-secret-with-minimum-secure-length";

    private TokenService tokenService;

    private User user;

    @BeforeEach
    void setUp() {
        tokenService =
                new TokenService();

        ReflectionTestUtils.setField(
                tokenService,
                "secret",
                SECRET
        );

        user =
                mock(
                        User.class
                );

        when(
                user.getLogin()
        ).thenReturn(
                "usuario@example.com"
        );

        when(
                user.getRole()
        ).thenReturn(
                UserRole.USER
        );
    }

    @Test
    void shouldGenerateValidToken() {
        String token =
                tokenService.generateToken(
                        user
                );

        assertThat(
                token
        ).isNotNull();

        assertThat(
                token
        ).isNotBlank();

        assertThat(
                token.split("\\.")
        ).hasSize(
                3
        );
    }

    @Test
    void shouldGenerateTokenWithExpectedIssuer() {
        String token =
                tokenService.generateToken(
                        user
                );

        DecodedJWT decodedToken =
                JWT.decode(
                        token
                );

        assertThat(
                decodedToken.getIssuer()
        ).isEqualTo(
                "apprh-api"
        );
    }

    @Test
    void shouldGenerateTokenWithUserLoginAsSubject() {
        String token =
                tokenService.generateToken(
                        user
                );

        DecodedJWT decodedToken =
                JWT.decode(
                        token
                );

        assertThat(
                decodedToken.getSubject()
        ).isEqualTo(
                "usuario@example.com"
        );
    }

    @Test
    void shouldGenerateTokenWithUserRole() {
        String token =
                tokenService.generateToken(
                        user
                );

        DecodedJWT decodedToken =
                JWT.decode(
                        token
                );

        String role =
                decodedToken
                        .getClaim(
                                "role"
                        )
                        .asString();

        assertThat(
                role
        ).isEqualTo(
                "USER"
        );
    }

    @Test
    void shouldGenerateTokenWithIssuedAtAndExpiration() {
        Instant beforeGeneration =
                Instant.now();

        String token =
                tokenService.generateToken(
                        user
                );

        Instant afterGeneration =
                Instant.now();

        DecodedJWT decodedToken =
                JWT.decode(
                        token
                );

        assertThat(
                decodedToken.getIssuedAtAsInstant()
        ).isBetween(
                beforeGeneration.minusSeconds(1),
                afterGeneration.plusSeconds(1)
        );

        assertThat(
                decodedToken.getExpiresAtAsInstant()
        ).isAfter(
                decodedToken.getIssuedAtAsInstant()
        );
    }

    @Test
    void shouldGenerateTokenWithExpectedExpirationTime() {
        String token =
                tokenService.generateToken(
                        user
                );

        DecodedJWT decodedToken =
                JWT.decode(
                        token
                );

        long expirationDifference =
                decodedToken
                        .getExpiresAtAsInstant()
                        .getEpochSecond()
                        - decodedToken
                        .getIssuedAtAsInstant()
                        .getEpochSecond();

        assertThat(
                expirationDifference
        ).isEqualTo(
                TokenService.EXPIRATION_SECONDS
        );
    }

    @Test
    void shouldValidateGeneratedTokenAndReturnLogin() {
        String token =
                tokenService.generateToken(
                        user
                );

        String login =
                tokenService.validateToken(
                        token
                );

        assertThat(
                login
        ).isEqualTo(
                "usuario@example.com"
        );
    }

    @Test
    void shouldReturnEmptyStringForInvalidToken() {
        String login =
                tokenService.validateToken(
                        "invalid-token"
                );

        assertThat(
                login
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringForBlankToken() {
        String login =
                tokenService.validateToken(
                        " "
                );

        assertThat(
                login
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringForNullToken() {
        String login =
                tokenService.validateToken(
                        null
                );

        assertThat(
                login
        ).isEmpty();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        TokenService anotherTokenService =
                new TokenService();

        ReflectionTestUtils.setField(
                anotherTokenService,
                "secret",
                "another-secret"
        );

        String token =
                tokenService.generateToken(
                        user
                );

        String login =
                anotherTokenService.validateToken(
                        token
                );

        assertThat(
                login
        ).isEmpty();
    }

    @Test
    void shouldReturnConfiguredExpirationSeconds() {
        assertThat(
                tokenService.getExpirationSeconds()
        ).isEqualTo(
                7200L
        );
    }
}