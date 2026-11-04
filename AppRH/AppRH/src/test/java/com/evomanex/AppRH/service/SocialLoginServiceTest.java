package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.identity.UserExternalIdentity;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.oauth.ExternalIdentityProvider;
import com.evomanex.AppRH.infra.security.oauth.OAuthAccountLinkException;
import com.evomanex.AppRH.infra.security.oauth.OAuthProviderProfile;
import com.evomanex.AppRH.repository.UserExternalIdentityRepository;
import com.evomanex.AppRH.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserExternalIdentityRepository
            externalIdentityRepository;

    @Mock
    private User user;

    @Mock
    private UserExternalIdentity externalIdentity;

    private SocialLoginService socialLoginService;

    @BeforeEach
    void setUp() {
        socialLoginService =
                new SocialLoginService(
                        userRepository,
                        externalIdentityRepository
                );
    }

    @Test
    void shouldReturnUserFromExistingExternalIdentity() {
        OAuthProviderProfile profile =
                googleProfile();

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GOOGLE,
                                "google-user-123"
                        )
        ).thenReturn(
                Optional.of(
                        externalIdentity
                )
        );

        when(
                externalIdentity.getUser()
        ).thenReturn(
                user
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.isAccountNonLocked()
        ).thenReturn(
                true
        );

        User result =
                socialLoginService.resolveUser(
                        profile
                );

        assertThat(
                result
        ).isSameAs(
                user
        );

        verify(
                externalIdentity
        ).registerLogin(
                "usuario@example.com",
                "Usuario Google",
                true
        );

        verify(
                userRepository,
                never()
        ).findByLoginIgnoreCase(
                "usuario@example.com"
        );

        verify(
                externalIdentityRepository,
                never()
        ).save(
                org.mockito.ArgumentMatchers
                        .any(
                                UserExternalIdentity.class
                        )
        );
    }

    @Test
    void shouldNormalizeEmailWhenUpdatingExistingIdentity() {
        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        ExternalIdentityProvider.GOOGLE,
                        "google-user-123",
                        "  USUARIO@EXAMPLE.COM  ",
                        true,
                        "Usuario Google"
                );

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GOOGLE,
                                "google-user-123"
                        )
        ).thenReturn(
                Optional.of(
                        externalIdentity
                )
        );

        when(
                externalIdentity.getUser()
        ).thenReturn(
                user
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.isAccountNonLocked()
        ).thenReturn(
                true
        );

        User result =
                socialLoginService.resolveUser(
                        profile
                );

        assertThat(
                result
        ).isSameAs(
                user
        );

        verify(
                externalIdentity
        ).registerLogin(
                "usuario@example.com",
                "Usuario Google",
                true
        );
    }

    @Test
    void shouldLinkExistingAppRhUserByEmail() {
        OAuthProviderProfile profile =
                githubProfile();

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GITHUB,
                                "github-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                userRepository.findByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.isAccountNonLocked()
        ).thenReturn(
                true
        );

        when(
                externalIdentityRepository
                        .findByUser_IdAndProvider(
                                "user-uuid-123",
                                ExternalIdentityProvider.GITHUB
                        )
        ).thenReturn(
                Optional.empty()
        );

        User result =
                socialLoginService.resolveUser(
                        profile
                );

        assertThat(
                result
        ).isSameAs(
                user
        );

        ArgumentCaptor<UserExternalIdentity> captor =
                ArgumentCaptor.forClass(
                        UserExternalIdentity.class
                );

        verify(
                externalIdentityRepository
        ).save(
                captor.capture()
        );

        UserExternalIdentity savedIdentity =
                captor.getValue();

        assertThat(
                savedIdentity.getUser()
        ).isSameAs(
                user
        );

        assertThat(
                savedIdentity.getProvider()
        ).isEqualTo(
                ExternalIdentityProvider.GITHUB
        );

        assertThat(
                savedIdentity.getProviderUserId()
        ).isEqualTo(
                "github-user-123"
        );

        assertThat(
                savedIdentity.getProviderEmail()
        ).isEqualTo(
                "usuario@example.com"
        );

        assertThat(
                savedIdentity.getProviderUsername()
        ).isEqualTo(
                "github-user"
        );

        assertThat(
                savedIdentity.isEmailVerified()
        ).isTrue();
    }

    @Test
    void shouldNormalizeEmailBeforeFindingLocalUser() {
        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        "  USUARIO@EXAMPLE.COM  ",
                        true,
                        "github-user"
                );

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GITHUB,
                                "github-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                userRepository.findByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.isAccountNonLocked()
        ).thenReturn(
                true
        );

        when(
                externalIdentityRepository
                        .findByUser_IdAndProvider(
                                "user-uuid-123",
                                ExternalIdentityProvider.GITHUB
                        )
        ).thenReturn(
                Optional.empty()
        );

        socialLoginService.resolveUser(
                profile
        );

        verify(
                userRepository
        ).findByLoginIgnoreCase(
                "usuario@example.com"
        );
    }

    @Test
    void shouldRejectNullProfile() {
        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                null
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "O perfil OAuth é obrigatório."
                );
    }

    @Test
    void shouldRejectProfileWithoutProvider() {
        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        null,
                        "provider-user-id",
                        "usuario@example.com",
                        true,
                        "usuario"
                );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "O provedor OAuth é obrigatório."
                );
    }

    @Test
    void shouldRejectProfileWithoutProviderUserId() {
        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        ExternalIdentityProvider.GOOGLE,
                        null,
                        "usuario@example.com",
                        true,
                        "usuario"
                );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "O provedor não informou o identificador do usuário."
                );
    }

    @Test
    void shouldRejectProfileWithBlankProviderUserId() {
        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        ExternalIdentityProvider.GOOGLE,
                        " ",
                        "usuario@example.com",
                        true,
                        "usuario"
                );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "O provedor não informou o identificador do usuário."
                );
    }

    @Test
    void shouldRejectMissingEmailDuringFirstLink() {
        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        null,
                        true,
                        "github-user"
                );

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GITHUB,
                                "github-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "O provedor não disponibilizou um e-mail."
                );
    }

    @Test
    void shouldRejectUnverifiedEmailDuringFirstLink() {
        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        ExternalIdentityProvider.GITHUB,
                        "github-user-123",
                        "usuario@example.com",
                        false,
                        "github-user"
                );

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GITHUB,
                                "github-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "O e-mail informado pelo provedor não está verificado."
                );
    }

    @Test
    void shouldRejectWhenLocalUserDoesNotExist() {
        OAuthProviderProfile profile =
                googleProfile();

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GOOGLE,
                                "google-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                userRepository.findByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "Não existe uma conta AppRH com esse e-mail."
                );
    }

    @Test
    void shouldRejectInactiveUser() {
        OAuthProviderProfile profile =
                googleProfile();

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GOOGLE,
                                "google-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                userRepository.findByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.isEnabled()
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "A conta AppRH não está ativa."
                );
    }

    @Test
    void shouldRejectLockedUser() {
        OAuthProviderProfile profile =
                googleProfile();

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GOOGLE,
                                "google-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                userRepository.findByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.isAccountNonLocked()
        ).thenReturn(
                false
        );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "A conta AppRH está bloqueada."
                );
    }

    @Test
    void shouldRejectDuplicateProviderLinkForUser() {
        OAuthProviderProfile profile =
                googleProfile();

        when(
                externalIdentityRepository
                        .findByProviderAndProviderUserId(
                                ExternalIdentityProvider.GOOGLE,
                                "google-user-123"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                userRepository.findByLoginIgnoreCase(
                        "usuario@example.com"
                )
        ).thenReturn(
                Optional.of(
                        user
                )
        );

        when(
                user.getId()
        ).thenReturn(
                "user-uuid-123"
        );

        when(
                user.isEnabled()
        ).thenReturn(
                true
        );

        when(
                user.isAccountNonLocked()
        ).thenReturn(
                true
        );

        when(
                externalIdentityRepository
                        .findByUser_IdAndProvider(
                                "user-uuid-123",
                                ExternalIdentityProvider.GOOGLE
                        )
        ).thenReturn(
                Optional.of(
                        externalIdentity
                )
        );

        assertThatThrownBy(
                () ->
                        socialLoginService.resolveUser(
                                profile
                        )
        )
                .isInstanceOf(
                        OAuthAccountLinkException.class
                )
                .hasMessage(
                        "A conta AppRH já está vinculada "
                                + "a outra identidade desse provedor."
                );

        verify(
                externalIdentityRepository,
                never()
        ).save(
                org.mockito.ArgumentMatchers
                        .any(
                                UserExternalIdentity.class
                        )
        );
    }

    private OAuthProviderProfile googleProfile() {
        return new OAuthProviderProfile(
                ExternalIdentityProvider.GOOGLE,
                "google-user-123",
                "usuario@example.com",
                true,
                "Usuario Google"
        );
    }

    private OAuthProviderProfile githubProfile() {
        return new OAuthProviderProfile(
                ExternalIdentityProvider.GITHUB,
                "github-user-123",
                "usuario@example.com",
                true,
                "github-user"
        );
    }
}