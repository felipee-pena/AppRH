package com.evomanex.AppRH.domain.identity;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.oauth.ExternalIdentityProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserExternalIdentityTest {

    private User user;

    @BeforeEach
    void setUp() {
        user =
                mock(
                        User.class
                );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );
    }

    @Test
    void shouldCreateGoogleIdentity() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "usuario@example.com",
                        "Usuario Google",
                        true
                );

        assertThat(
                identity.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                identity.getProvider()
        ).isEqualTo(
                ExternalIdentityProvider.GOOGLE
        );

        assertThat(
                identity.getProviderUserId()
        ).isEqualTo(
                "google-user-123"
        );

        assertThat(
                identity.getProviderEmail()
        ).isEqualTo(
                "usuario@example.com"
        );

        assertThat(
                identity.getProviderUsername()
        ).isEqualTo(
                "Usuario Google"
        );

        assertThat(
                identity.isEmailVerified()
        ).isTrue();
    }

    @Test
    void shouldCreateGitHubIdentity() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        "usuario@example.com",
                        "github-user",
                        true
                );

        assertThat(
                identity.getProvider()
        ).isEqualTo(
                ExternalIdentityProvider.GITHUB
        );

        assertThat(
                identity.getProviderUserId()
        ).isEqualTo(
                "github-user-123"
        );
    }

    @Test
    void shouldNormalizeProviderEmail() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        "  USUARIO@EXAMPLE.COM  ",
                        "github-user",
                        true
                );

        assertThat(
                identity.getProviderEmail()
        ).isEqualTo(
                "usuario@example.com"
        );
    }

    @Test
    void shouldTrimProviderUserIdAndUsername() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "  github-user-123  ",
                        "usuario@example.com",
                        "  github-user  ",
                        true
                );

        assertThat(
                identity.getProviderUserId()
        ).isEqualTo(
                "github-user-123"
        );

        assertThat(
                identity.getProviderUsername()
        ).isEqualTo(
                "github-user"
        );
    }

    @Test
    void shouldConvertBlankEmailToNull() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        " ",
                        "github-user",
                        false
                );

        assertThat(
                identity.getProviderEmail()
        ).isNull();
    }

    @Test
    void shouldConvertBlankUsernameToNull() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        "usuario@example.com",
                        " ",
                        false
                );

        assertThat(
                identity.getProviderUsername()
        ).isNull();
    }

    @Test
    void shouldAcceptNullEmailAndUsername() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        null,
                        null,
                        false
                );

        assertThat(
                identity.getProviderEmail()
        ).isNull();

        assertThat(
                identity.getProviderUsername()
        ).isNull();

        assertThat(
                identity.isEmailVerified()
        ).isFalse();
    }

    @Test
    void shouldUpdateIdentityWhenLoginIsRegistered() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        "old@example.com",
                        "old-user",
                        false
                );

        identity.registerLogin(
                "  NEW@EXAMPLE.COM  ",
                "  new-user  ",
                true
        );

        assertThat(
                identity.getProviderEmail()
        ).isEqualTo(
                "new@example.com"
        );

        assertThat(
                identity.getProviderUsername()
        ).isEqualTo(
                "new-user"
        );

        assertThat(
                identity.isEmailVerified()
        ).isTrue();

        assertThat(
                identity.getLastLoginAt()
        ).isNotNull();
    }

    @Test
    void shouldInitializeTimestampsBeforePersistence() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "usuario@example.com",
                        "usuario",
                        true
                );

        ReflectionTestUtils.invokeMethod(
                identity,
                "prePersist"
        );

        assertThat(
                identity.getCreatedAt()
        ).isNotNull();

        assertThat(
                identity.getUpdatedAt()
        ).isNotNull();

        assertThat(
                identity.getLastLoginAt()
        ).isNotNull();
    }

    @Test
    void shouldUpdateTimestampBeforeUpdate() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "usuario@example.com",
                        "usuario",
                        true
                );

        OffsetDateTime oldUpdatedAt =
                OffsetDateTime.now()
                        .minusHours(1);

        ReflectionTestUtils.setField(
                identity,
                "updatedAt",
                oldUpdatedAt
        );

        ReflectionTestUtils.invokeMethod(
                identity,
                "preUpdate"
        );

        assertThat(
                identity.getUpdatedAt()
        ).isAfter(
                oldUpdatedAt
        );
    }

    @Test
    void shouldRejectNullUser() {
        assertThatThrownBy(
                () ->
                        new UserExternalIdentity(
                                null,
                                ExternalIdentityProvider.GOOGLE,
                                "provider-id",
                                "usuario@example.com",
                                "usuario",
                                true
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O usuário AppRH é obrigatório."
                );
    }

    @Test
    void shouldRejectUserWithoutIdentifier() {
        when(
                user.getId()
        ).thenReturn(
                null
        );

        assertThatThrownBy(
                () ->
                        new UserExternalIdentity(
                                user,
                                ExternalIdentityProvider.GOOGLE,
                                "provider-id",
                                "usuario@example.com",
                                "usuario",
                                true
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O usuário AppRH não possui identificador válido."
                );
    }

    @Test
    void shouldRejectUserWithBlankIdentifier() {
        when(
                user.getId()
        ).thenReturn(
                " "
        );

        assertThatThrownBy(
                () ->
                        new UserExternalIdentity(
                                user,
                                ExternalIdentityProvider.GOOGLE,
                                "provider-id",
                                "usuario@example.com",
                                "usuario",
                                true
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O usuário AppRH não possui identificador válido."
                );
    }

    @Test
    void shouldRejectNullProvider() {
        assertThatThrownBy(
                () ->
                        new UserExternalIdentity(
                                user,
                                null,
                                "provider-id",
                                "usuario@example.com",
                                "usuario",
                                true
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O provedor OAuth é obrigatório."
                );
    }

    @Test
    void shouldRejectNullProviderUserId() {
        assertThatThrownBy(
                () ->
                        new UserExternalIdentity(
                                user,
                                ExternalIdentityProvider.GOOGLE,
                                null,
                                "usuario@example.com",
                                "usuario",
                                true
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O identificador do usuário no provedor é obrigatório."
                );
    }

    @Test
    void shouldRejectBlankProviderUserId() {
        assertThatThrownBy(
                () ->
                        new UserExternalIdentity(
                                user,
                                ExternalIdentityProvider.GOOGLE,
                                " ",
                                "usuario@example.com",
                                "usuario",
                                true
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O identificador do usuário no provedor é obrigatório."
                );
    }

    @Test
    void shouldBeEqualWhenReferencesAreSame() {
        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "usuario@example.com",
                        "usuario",
                        true
                );

        assertThat(
                identity
        ).isEqualTo(
                identity
        );
    }

    @Test
    void shouldNotBeEqualWhenIdentifiersAreNull() {
        UserExternalIdentity firstIdentity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "usuario@example.com",
                        "usuario",
                        true
                );

        UserExternalIdentity secondIdentity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        "usuario@example.com",
                        "usuario",
                        true
                );

        assertThat(
                firstIdentity
        ).isNotEqualTo(
                secondIdentity
        );
    }

    @Test
    void shouldBeEqualWhenIdentifiersAreEqual() {
        UserExternalIdentity firstIdentity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "usuario@example.com",
                        "usuario",
                        true
                );

        UserExternalIdentity secondIdentity =
                new UserExternalIdentity(
                        user,
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "usuario@example.com",
                        "usuario",
                        true
                );

        ReflectionTestUtils.setField(
                firstIdentity,
                "id",
                10L
        );

        ReflectionTestUtils.setField(
                secondIdentity,
                "id",
                10L
        );

        assertThat(
                firstIdentity
        ).isEqualTo(
                secondIdentity
        );

        assertThat(
                firstIdentity.hashCode()
        ).isEqualTo(
                secondIdentity.hashCode()
        );
    }
}