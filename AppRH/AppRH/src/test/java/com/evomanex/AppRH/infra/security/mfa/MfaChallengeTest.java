package com.evomanex.AppRH.infra.security.mfa;

import com.evomanex.AppRH.domain.mfa.MfaChallenge;
import com.evomanex.AppRH.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MfaChallengeTest {

    private User user;

    @BeforeEach
    void setUp() {
        user =
                mock(
                        User.class
                );
    }

    @Test
    void shouldCreateAvailableChallenge() {
        OffsetDateTime now =
                OffsetDateTime.now();

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        now.plusMinutes(5)
                );

        assertThat(
                challenge.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                challenge.isAvailable(
                        now
                )
        ).isTrue();
    }

    @Test
    void shouldRejectExpiredChallenge() {
        OffsetDateTime now =
                OffsetDateTime.now();

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        now.minusSeconds(1)
                );

        assertThat(
                challenge.isAvailable(
                        now
                )
        ).isFalse();
    }

    @Test
    void shouldRejectChallengeExpiringAtCurrentTime() {
        OffsetDateTime now =
                OffsetDateTime.now();

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        now
                );

        assertThat(
                challenge.isAvailable(
                        now
                )
        ).isFalse();
    }

    @Test
    void shouldRejectConsumedChallenge() {
        OffsetDateTime now =
                OffsetDateTime.now();

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        now.plusMinutes(5)
                );

        challenge.consume();

        OffsetDateTime consumedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        challenge,
                        "consumedAt"
                );

        assertThat(
                challenge.isAvailable(
                        now
                )
        ).isFalse();

        assertThat(
                consumedAt
        ).isNotNull();
    }

    @Test
    void shouldRegisterOneFailure() {
        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        OffsetDateTime.now()
                                .plusMinutes(5)
                );

        challenge.registerFailure();

        Integer failedAttempts =
                (Integer) ReflectionTestUtils.getField(
                        challenge,
                        "failedAttempts"
                );

        assertThat(
                failedAttempts
        ).isEqualTo(
                1
        );
    }

    @Test
    void shouldRemainAvailableBeforeFiveFailures() {
        OffsetDateTime now =
                OffsetDateTime.now();

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        now.plusMinutes(5)
                );

        for (
                int index = 0;
                index < 4;
                index++
        ) {
            challenge.registerFailure();
        }

        Integer failedAttempts =
                (Integer) ReflectionTestUtils.getField(
                        challenge,
                        "failedAttempts"
                );

        assertThat(
                failedAttempts
        ).isEqualTo(
                4
        );

        assertThat(
                challenge.isAvailable(
                        now
                )
        ).isTrue();
    }

    @Test
    void shouldBlockChallengeAfterFiveFailures() {
        OffsetDateTime now =
                OffsetDateTime.now();

        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        now.plusMinutes(5)
                );

        for (
                int index = 0;
                index < 5;
                index++
        ) {
            challenge.registerFailure();
        }

        Integer failedAttempts =
                (Integer) ReflectionTestUtils.getField(
                        challenge,
                        "failedAttempts"
                );

        assertThat(
                failedAttempts
        ).isEqualTo(
                5
        );

        assertThat(
                challenge.isAvailable(
                        now
                )
        ).isFalse();
    }

    @Test
    void shouldNotExceedMaximumFailures() {
        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        OffsetDateTime.now()
                                .plusMinutes(5)
                );

        for (
                int index = 0;
                index < 10;
                index++
        ) {
            challenge.registerFailure();
        }

        Integer failedAttempts =
                (Integer) ReflectionTestUtils.getField(
                        challenge,
                        "failedAttempts"
                );

        assertThat(
                failedAttempts
        ).isEqualTo(
                5
        );
    }

    @Test
    void shouldInitializeCreationDateBeforePersistence() {
        MfaChallenge challenge =
                new MfaChallenge(
                        user,
                        "challenge-hash",
                        OffsetDateTime.now()
                                .plusMinutes(5)
                );

        OffsetDateTime beforePersistence =
                OffsetDateTime.now();

        ReflectionTestUtils.invokeMethod(
                challenge,
                "prePersist"
        );

        OffsetDateTime afterPersistence =
                OffsetDateTime.now();

        OffsetDateTime createdAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        challenge,
                        "createdAt"
                );

        assertThat(
                createdAt
        ).isNotNull();

        assertThat(
                createdAt
        ).isBetween(
                beforePersistence,
                afterPersistence
        );
    }
}