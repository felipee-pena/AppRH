package com.evomanex.AppRH.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.evomanex.AppRH.domain.user.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    public static final long EXPIRATION_SECONDS = 7200L;

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm =
                    Algorithm.HMAC256(secret);

            return JWT
                    .create()
                    .withIssuer("apprh-api")
                    .withSubject(user.getLogin())
                    .withClaim(
                            "role",
                            user.getRole().name()
                    )
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(
                            generateExpirationDate()
                    )
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new RuntimeException(
                    "Erro ao gerar token JWT",
                    exception
            );
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm =
                    Algorithm.HMAC256(secret);

            return JWT
                    .require(algorithm)
                    .withIssuer("apprh-api")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime
                .now()
                .plusSeconds(EXPIRATION_SECONDS)
                .toInstant(
                        ZoneOffset.of("-03:00")
                );
    }
}