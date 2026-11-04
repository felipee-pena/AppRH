package com.evomanex.AppRH.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.evomanex.AppRH.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    public static final long EXPIRATION_SECONDS =
            7200L;

    private static final String ISSUER =
            "apprh-api";

    @Value(
            "${api.security.token.secret}"
    )
    private String secret;

    public String generateToken(
            User user
    ) {
        try {
            Algorithm algorithm =
                    Algorithm.HMAC256(
                            secret
                    );

            return JWT
                    .create()
                    .withIssuer(
                            ISSUER
                    )
                    .withSubject(
                            user.getLogin()
                    )
                    .withClaim(
                            "role",
                            user.getRole().name()
                    )
                    .withIssuedAt(
                            Instant.now()
                    )
                    .withExpiresAt(
                            generateExpirationDate()
                    )
                    .sign(
                            algorithm
                    );
        } catch (
                JWTCreationException exception
        ) {
            throw new IllegalStateException(
                    "Erro ao gerar token JWT.",
                    exception
            );
        }
    }

    public String validateToken(
            String token
    ) {
        if (
                token == null
                        || token.isBlank()
        ) {
            return "";
        }

        try {
            Algorithm algorithm =
                    Algorithm.HMAC256(
                            secret
                    );

            return JWT
                    .require(
                            algorithm
                    )
                    .withIssuer(
                            ISSUER
                    )
                    .build()
                    .verify(
                            token
                    )
                    .getSubject();
        } catch (
                JWTVerificationException exception
        ) {
            return "";
        }
    }

    public long getExpirationSeconds() {
        return EXPIRATION_SECONDS;
    }

    private Instant generateExpirationDate() {
        return Instant
                .now()
                .plusSeconds(
                        EXPIRATION_SECONDS
                );
    }
}