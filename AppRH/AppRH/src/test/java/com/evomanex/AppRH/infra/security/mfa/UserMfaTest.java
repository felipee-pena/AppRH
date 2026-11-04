package com.evomanex.AppRH.infra.security.mfa;

import com.evomanex.AppRH.domain.mfa.UserMfa;
import com.evomanex.AppRH.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UserMfaTest {

    private User user;

    private UserMfa userMfa;

    @BeforeEach
    void setUp() {
        user =
                mock(
                        User.class
                );

        userMfa =
                new UserMfa(
                        user,
                        MfaMethod.TOTP,
                        "encrypted-secret"
                );
    }

    @Test
    void shouldCreateDisabledMfaConfiguration() {
        assertThat(
                userMfa.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                userMfa.getSecretEncrypted()
        ).isEqualTo(
                "encrypted-secret"
        );

        assertThat(
                userMfa.isEnabled()
        ).isFalse();
    }

    @Test
    void shouldConfirmMfaConfiguration() {
        OffsetDateTime beforeConfirmation =
                OffsetDateTime.now();

        userMfa.confirm();

        OffsetDateTime afterConfirmation =
                OffsetDateTime.now();

        OffsetDateTime confirmedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        userMfa,
                        "confirmedAt"
                );

        assertThat(
                userMfa.isEnabled()
        ).isTrue();

        assertThat(
                confirmedAt
        ).isNotNull();

        assertThat(
                confirmedAt
        ).isBetween(
                beforeConfirmation,
                afterConfirmation
        );
    }

    @Test
    void shouldDisableMfaConfiguration() {
        userMfa.confirm();

        userMfa.disable();

        OffsetDateTime confirmedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        userMfa,
                        "confirmedAt"
                );

        assertThat(
                userMfa.isEnabled()
        ).isFalse();

        assertThat(
                confirmedAt
        ).isNull();
    }

    @Test
    void shouldRegisterLastUse() {
        OffsetDateTime beforeUse =
                OffsetDateTime.now();

        userMfa.registerUse();

        OffsetDateTime afterUse =
                OffsetDateTime.now();

        OffsetDateTime lastUsedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        userMfa,
                        "lastUsedAt"
                );

        assertThat(
                lastUsedAt
        ).isNotNull();

        assertThat(
                lastUsedAt
        ).isBetween(
                beforeUse,
                afterUse
        );
    }

    @Test
    void shouldInitializeTimestampsBeforePersistence() {
        OffsetDateTime beforePersistence =
                OffsetDateTime.now();

        ReflectionTestUtils.invokeMethod(
                userMfa,
                "prePersist"
        );

        OffsetDateTime afterPersistence =
                OffsetDateTime.now();

        OffsetDateTime createdAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        userMfa,
                        "createdAt"
                );

        OffsetDateTime updatedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        userMfa,
                        "updatedAt"
                );

        assertThat(
                createdAt
        ).isNotNull();

        assertThat(
                updatedAt
        ).isNotNull();

        assertThat(
                createdAt
        ).isBetween(
                beforePersistence,
                afterPersistence
        );

        assertThat(
                updatedAt
        ).isBetween(
                beforePersistence,
                afterPersistence
        );
    }

    @Test
    void shouldUpdateTimestampBeforeUpdate() {
        OffsetDateTime oldUpdatedAt =
                OffsetDateTime.now()
                        .minusHours(1);

        ReflectionTestUtils.setField(
                userMfa,
                "updatedAt",
                oldUpdatedAt
        );

        ReflectionTestUtils.invokeMethod(
                userMfa,
                "preUpdate"
        );

        OffsetDateTime updatedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        userMfa,
                        "updatedAt"
                );

        assertThat(
                updatedAt
        ).isNotNull();

        assertThat(
                updatedAt
        ).isAfter(
                oldUpdatedAt
        );
    }
}