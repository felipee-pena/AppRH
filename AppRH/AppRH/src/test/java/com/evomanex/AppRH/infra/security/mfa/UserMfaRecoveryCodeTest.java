package com.evomanex.AppRH.infra.security.mfa;

import com.evomanex.AppRH.domain.mfa.UserMfaRecoveryCode;
import com.evomanex.AppRH.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UserMfaRecoveryCodeTest {

    private User user;

    @BeforeEach
    void setUp() {
        user =
                mock(
                        User.class
                );
    }

    @Test
    void shouldCreateAvailableRecoveryCode() {
        UserMfaRecoveryCode recoveryCode =
                new UserMfaRecoveryCode(
                        user,
                        "recovery-code-hash"
                );

        assertThat(
                recoveryCode.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                recoveryCode.getCodeHash()
        ).isEqualTo(
                "recovery-code-hash"
        );

        assertThat(
                recoveryCode.isAvailable()
        ).isTrue();

        assertThat(
                recoveryCode.getUsedAt()
        ).isNull();
    }

    @Test
    void shouldConsumeRecoveryCode() {
        OffsetDateTime beforeConsumption =
                OffsetDateTime.now();

        UserMfaRecoveryCode recoveryCode =
                new UserMfaRecoveryCode(
                        user,
                        "recovery-code-hash"
                );

        recoveryCode.consume();

        OffsetDateTime afterConsumption =
                OffsetDateTime.now();

        assertThat(
                recoveryCode.isAvailable()
        ).isFalse();

        assertThat(
                recoveryCode.getUsedAt()
        ).isNotNull();

        assertThat(
                recoveryCode.getUsedAt()
        ).isBetween(
                beforeConsumption,
                afterConsumption
        );
    }

    @Test
    void shouldRejectSecondConsumption() {
        UserMfaRecoveryCode recoveryCode =
                new UserMfaRecoveryCode(
                        user,
                        "recovery-code-hash"
                );

        recoveryCode.consume();

        assertThatThrownBy(
                recoveryCode::consume
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O código de recuperação já foi utilizado."
                );
    }

    @Test
    void shouldInitializeCreationDateBeforePersistence() {
        UserMfaRecoveryCode recoveryCode =
                new UserMfaRecoveryCode(
                        user,
                        "recovery-code-hash"
                );

        OffsetDateTime beforePersistence =
                OffsetDateTime.now();

        ReflectionTestUtils.invokeMethod(
                recoveryCode,
                "prePersist"
        );

        OffsetDateTime afterPersistence =
                OffsetDateTime.now();

        assertThat(
                recoveryCode.getCreatedAt()
        ).isNotNull();

        assertThat(
                recoveryCode.getCreatedAt()
        ).isBetween(
                beforePersistence,
                afterPersistence
        );
    }

    @Test
    void shouldRejectNullUser() {
        assertThatThrownBy(
                () ->
                        new UserMfaRecoveryCode(
                                null,
                                "recovery-code-hash"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O usuário é obrigatório."
                );
    }

    @Test
    void shouldRejectNullHash() {
        assertThatThrownBy(
                () ->
                        new UserMfaRecoveryCode(
                                user,
                                null
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O hash do código de recuperação é obrigatório."
                );
    }

    @Test
    void shouldRejectBlankHash() {
        assertThatThrownBy(
                () ->
                        new UserMfaRecoveryCode(
                                user,
                                " "
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O hash do código de recuperação é obrigatório."
                );
    }
}