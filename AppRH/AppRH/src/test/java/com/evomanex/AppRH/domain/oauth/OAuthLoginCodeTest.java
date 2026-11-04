package com.evomanex.AppRH.domain.oauth;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.oauth.OAuthLoginCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OAuthLoginCodeTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
    }

    @Test
    void shouldCreateAvailableOAuthCode() {
        OffsetDateTime now =
                OffsetDateTime.now();

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        now.plusMinutes(2)
                );

        assertThat(
                loginCode.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                loginCode.isAvailable(now)
        ).isTrue();
    }

    @Test
    void shouldRejectExpiredOAuthCode() {
        OffsetDateTime now =
                OffsetDateTime.now();

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        now.minusSeconds(1)
                );

        assertThat(
                loginCode.isAvailable(now)
        ).isFalse();
    }

    @Test
    void shouldRejectOAuthCodeExpiringAtCurrentTime() {
        OffsetDateTime now =
                OffsetDateTime.now();

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        now
                );

        assertThat(
                loginCode.isAvailable(now)
        ).isFalse();
    }

    @Test
    void shouldRemainAvailableBeforeExpiration() {
        OffsetDateTime now =
                OffsetDateTime.now();

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        now.plusSeconds(30)
                );

        assertThat(
                loginCode.isAvailable(
                        now.plusSeconds(20)
                )
        ).isTrue();
    }

    @Test
    void shouldBecomeUnavailableAfterExpiration() {
        OffsetDateTime now =
                OffsetDateTime.now();

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        now.plusSeconds(30)
                );

        assertThat(
                loginCode.isAvailable(
                        now.plusSeconds(31)
                )
        ).isFalse();
    }

    @Test
    void shouldBecomeUnavailableAfterConsumption() {
        OffsetDateTime now =
                OffsetDateTime.now();

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        now.plusMinutes(2)
                );

        assertThat(
                loginCode.isAvailable(now)
        ).isTrue();

        loginCode.consume();

        assertThat(
                loginCode.isAvailable(now)
        ).isFalse();
    }

    @Test
    void shouldSetConsumptionDate() {
        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        OffsetDateTime.now()
                                .plusMinutes(2)
                );

        OffsetDateTime beforeConsumption =
                OffsetDateTime.now();

        loginCode.consume();

        OffsetDateTime afterConsumption =
                OffsetDateTime.now();

        OffsetDateTime consumedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        loginCode,
                        "consumedAt"
                );

        assertThat(
                consumedAt
        )
                .isNotNull()
                .isBetween(
                        beforeConsumption,
                        afterConsumption
                );
    }

    @Test
    void shouldInitializeCreationDateBeforePersistence() {
        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        OffsetDateTime.now()
                                .plusMinutes(2)
                );

        OffsetDateTime beforePersistence =
                OffsetDateTime.now();

        ReflectionTestUtils.invokeMethod(
                loginCode,
                "prePersist"
        );

        OffsetDateTime afterPersistence =
                OffsetDateTime.now();

        OffsetDateTime createdAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        loginCode,
                        "createdAt"
                );

        assertThat(
                createdAt
        )
                .isNotNull()
                .isBetween(
                        beforePersistence,
                        afterPersistence
                );
    }

    @Test
    void shouldStoreCodeHash() {
        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        OffsetDateTime.now()
                                .plusMinutes(2)
                );

        String codeHash =
                (String) ReflectionTestUtils.getField(
                        loginCode,
                        "codeHash"
                );

        assertThat(
                codeHash
        ).isEqualTo(
                "oauth-code-hash"
        );
    }

    @Test
    void shouldStoreExpirationDate() {
        OffsetDateTime expiration =
                OffsetDateTime.now()
                        .plusMinutes(2);

        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        expiration
                );

        OffsetDateTime storedExpiration =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        loginCode,
                        "expiresAt"
                );

        assertThat(
                storedExpiration
        ).isEqualTo(
                expiration
        );
    }

    @Test
    void shouldStartWithoutConsumptionDate() {
        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        OffsetDateTime.now()
                                .plusMinutes(2)
                );

        OffsetDateTime consumedAt =
                (OffsetDateTime) ReflectionTestUtils.getField(
                        loginCode,
                        "consumedAt"
                );

        assertThat(
                consumedAt
        ).isNull();
    }

    @Test
    void shouldKeepAssociatedUser() {
        OAuthLoginCode loginCode =
                new OAuthLoginCode(
                        user,
                        "oauth-code-hash",
                        OffsetDateTime.now()
                                .plusMinutes(2)
                );

        assertThat(
                loginCode.getUser()
        ).isSameAs(
                user
        );
    }
}