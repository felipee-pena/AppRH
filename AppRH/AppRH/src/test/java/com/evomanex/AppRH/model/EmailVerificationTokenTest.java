package com.evomanex.AppRH.model;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationTokenTest {

    @Test
    void shouldCreateUnusedAndValidToken() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        OffsetDateTime expiration =
                OffsetDateTime
                        .now()
                        .plusHours(24);

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        "a".repeat(64),
                        expiration
                );

        assertThat(
                token.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                token.getTokenHash()
        ).isEqualTo(
                "a".repeat(64)
        );

        assertThat(
                token.getExpiresAt()
        ).isEqualTo(
                expiration
        );

        assertThat(
                token.getUsedAt()
        ).isNull();

        assertThat(
                token.isUsed()
        ).isFalse();

        assertThat(
                token.isExpired()
        ).isFalse();
    }

    @Test
    void shouldIdentifyExpiredToken() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        "b".repeat(64),
                        OffsetDateTime
                                .now()
                                .minusMinutes(1)
                );

        assertThat(
                token.isExpired()
        ).isTrue();
    }

    @Test
    void shouldMarkTokenAsUsed() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        "c".repeat(64),
                        OffsetDateTime
                                .now()
                                .plusHours(1)
                );

        token.markAsUsed();

        assertThat(
                token.isUsed()
        ).isTrue();

        assertThat(
                token.getUsedAt()
        ).isNotNull();
    }

    @Test
    void shouldInvalidateUnusedToken() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        "d".repeat(64),
                        OffsetDateTime
                                .now()
                                .plusHours(1)
                );

        token.invalidate();

        assertThat(
                token.isUsed()
        ).isTrue();

        assertThat(
                token.getUsedAt()
        ).isNotNull();
    }

    @Test
    void shouldNotChangeUsedAtWhenInvalidatingAlreadyUsedToken() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        "e".repeat(64),
                        OffsetDateTime
                                .now()
                                .plusHours(1)
                );

        token.markAsUsed();

        OffsetDateTime firstUsedAt =
                token.getUsedAt();

        token.invalidate();

        assertThat(
                token.getUsedAt()
        ).isEqualTo(
                firstUsedAt
        );
    }

    @Test
    void shouldHaveNullIdBeforePersistence() {
        User user =
                new User(
                        "usuario@example.com",
                        "$2a$10$encoded",
                        UserRole.USER
                );

        EmailVerificationToken token =
                new EmailVerificationToken(
                        user,
                        "f".repeat(64),
                        OffsetDateTime
                                .now()
                                .plusHours(1)
                );

        assertThat(
                token.getId()
        ).isNull();
    }
}