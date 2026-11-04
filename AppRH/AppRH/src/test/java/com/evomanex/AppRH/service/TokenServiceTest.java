package com.evomanex.AppRH.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;
import com.evomanex.AppRH.infra.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private static final String SECRET =
            "test-secret-with-minimum-secure-length";

    private static final String DIFFERENT_SECRET =
            "different-secret-with-minimum-length";

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
    void shouldGenerateTokenWithExpectedClaims() {
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

        assertThat(
                decodedToken.getSubject()
        ).isEqualTo(
                "usuario@example.com"
        );

        assertThat(
                decodedToken
                        .getClaim("role")
                        .asString()
        ).isEqualTo(
                "USER"
        );

        assertThat(
                decodedToken.getIssuedAtAsInstant()
        ).isNotNull();

        assertThat(
                decodedToken.getExpiresAtAsInstant()
        ).isNotNull();

        assertThat(
                decodedToken.getExpiresAtAsInstant()
        ).isAfter(
                decodedToken.getIssuedAtAsInstant()
        );
    }

    @Test
    void shouldGenerateTokenWithExpectedExpiration() {
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

        Instant minimumExpectedExpiration =
                beforeGeneration.plusSeconds(
                        TokenService.EXPIRATION_SECONDS
                );

        Instant maximumExpectedExpiration =
                afterGeneration.plusSeconds(
                        TokenService.EXPIRATION_SECONDS
                );

        assertThat(
                decodedToken.getExpiresAtAsInstant()
        ).isBetween(
                minimumExpectedExpiration.minusSeconds(1),
                maximumExpectedExpiration.plusSeconds(1)
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
    void shouldRejectTamperedToken() {
        String validToken =
                tokenService.generateToken(
                        user
                );

        String[] tokenParts =
                validToken.split(
                        "\\."
                );

        assertThat(
                tokenParts
        ).hasSize(
                3
        );

        String tamperedPayloadJson =
                """
                {
                  "iss": "apprh-api",
                  "sub": "invasor@example.com",
                  "role": "ADMIN"
                }
                """;

        String tamperedPayload =
                Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                tamperedPayloadJson.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        String tamperedToken =
                tokenParts[0]
                        + "."
                        + tamperedPayload
                        + "."
                        + tokenParts[2];

        String login =
                tokenService.validateToken(
                        tamperedToken
                );

        assertThat(
                login
        ).isEmpty();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        TokenService differentTokenService =
                new TokenService();

        ReflectionTestUtils.setField(
                differentTokenService,
                "secret",
                DIFFERENT_SECRET
        );

        String token =
                tokenService.generateToken(
                        user
                );

        String login =
                differentTokenService.validateToken(
                        token
                );

        assertThat(
                login
        ).isEmpty();
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
    void shouldReturnExpirationSeconds() {
        assertThat(
                tokenService.getExpirationSeconds()
        ).isEqualTo(
                TokenService.EXPIRATION_SECONDS
        );

        assertThat(
                tokenService.getExpirationSeconds()
        ).isEqualTo(
                7200L
        );
    }
}