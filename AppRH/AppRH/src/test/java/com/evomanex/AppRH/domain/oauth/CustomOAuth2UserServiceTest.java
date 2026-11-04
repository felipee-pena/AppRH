package com.evomanex.AppRH.domain.oauth;

import com.evomanex.AppRH.infra.security.oauth.CustomOAuth2UserService;
import com.evomanex.AppRH.infra.security.oauth.GitHubEmailService;
import com.evomanex.AppRH.service.SocialLoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CustomOAuth2UserServiceTest {

    private SocialLoginService socialLoginService;

    private GitHubEmailService githubEmailService;

    private OAuth2User oauth2User;

    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        socialLoginService =
                mock(
                        SocialLoginService.class
                );

        githubEmailService =
                mock(
                        GitHubEmailService.class
                );

        oauth2User =
                mock(
                        OAuth2User.class
                );

        service =
                new CustomOAuth2UserService(
                        socialLoginService,
                        githubEmailService
                );
    }

    @Test
    void shouldConvertIntegerGitHubIdToString() {
        doReturn(
                12345678
        ).when(
                oauth2User
        ).getAttribute(
                "id"
        );

        String result =
                service.extractProviderUserId(
                        oauth2User
                );

        assertThat(
                result
        ).isEqualTo(
                "12345678"
        );
    }

    @Test
    void shouldConvertLongGitHubIdToString() {
        doReturn(
                9876543210L
        ).when(
                oauth2User
        ).getAttribute(
                "id"
        );

        String result =
                service.extractProviderUserId(
                        oauth2User
                );

        assertThat(
                result
        ).isEqualTo(
                "9876543210"
        );
    }

    @Test
    void shouldPreserveStringProviderId() {
        doReturn(
                "provider-user-123"
        ).when(
                oauth2User
        ).getAttribute(
                "id"
        );

        String result =
                service.extractProviderUserId(
                        oauth2User
                );

        assertThat(
                result
        ).isEqualTo(
                "provider-user-123"
        );
    }

    @Test
    void shouldTrimProviderId() {
        doReturn(
                "  provider-user-123  "
        ).when(
                oauth2User
        ).getAttribute(
                "id"
        );

        String result =
                service.extractProviderUserId(
                        oauth2User
                );

        assertThat(
                result
        ).isEqualTo(
                "provider-user-123"
        );
    }

    @Test
    void shouldRejectMissingProviderId() {
        doReturn(
                null
        ).when(
                oauth2User
        ).getAttribute(
                "id"
        );

        assertThatThrownBy(
                () ->
                        service.extractProviderUserId(
                                oauth2User
                        )
        )
                .isInstanceOf(
                        OAuth2AuthenticationException.class
                )
                .hasMessageContaining(
                        "O GitHub não informou "
                                + "o identificador do usuário."
                );
    }

    @Test
    void shouldRejectBlankProviderId() {
        doReturn(
                " "
        ).when(
                oauth2User
        ).getAttribute(
                "id"
        );

        assertThatThrownBy(
                () ->
                        service.extractProviderUserId(
                                oauth2User
                        )
        )
                .isInstanceOf(
                        OAuth2AuthenticationException.class
                )
                .hasMessageContaining(
                        "O GitHub não informou "
                                + "o identificador do usuário."
                );
    }

    @Test
    void shouldRejectNullOAuthUser() {
        assertThatThrownBy(
                () ->
                        service.extractProviderUserId(
                                null
                        )
        )
                .isInstanceOf(
                        OAuth2AuthenticationException.class
                )
                .hasMessageContaining(
                        "O usuário OAuth é obrigatório."
                );
    }

    @Test
    void shouldExtractAndTrimEmailAttribute() {
        doReturn(
                "  usuario@example.com  "
        ).when(
                oauth2User
        ).getAttribute(
                "email"
        );

        String result =
                service.extractTextAttribute(
                        oauth2User,
                        "email"
                );

        assertThat(
                result
        ).isEqualTo(
                "usuario@example.com"
        );
    }

    @Test
    void shouldExtractAndTrimUsernameAttribute() {
        doReturn(
                "  github-user  "
        ).when(
                oauth2User
        ).getAttribute(
                "login"
        );

        String result =
                service.extractTextAttribute(
                        oauth2User,
                        "login"
                );

        assertThat(
                result
        ).isEqualTo(
                "github-user"
        );
    }

    @Test
    void shouldConvertNumericTextAttributeToString() {
        doReturn(
                12345
        ).when(
                oauth2User
        ).getAttribute(
                "customAttribute"
        );

        String result =
                service.extractTextAttribute(
                        oauth2User,
                        "customAttribute"
                );

        assertThat(
                result
        ).isEqualTo(
                "12345"
        );
    }

    @Test
    void shouldReturnNullForMissingTextAttribute() {
        doReturn(
                null
        ).when(
                oauth2User
        ).getAttribute(
                "email"
        );

        String result =
                service.extractTextAttribute(
                        oauth2User,
                        "email"
                );

        assertThat(
                result
        ).isNull();
    }

    @Test
    void shouldReturnNullForBlankTextAttribute() {
        doReturn(
                " "
        ).when(
                oauth2User
        ).getAttribute(
                "email"
        );

        String result =
                service.extractTextAttribute(
                        oauth2User,
                        "email"
                );

        assertThat(
                result
        ).isNull();
    }

    @Test
    void shouldReturnNullForNullOAuthUserWhenExtractingText() {
        String result =
                service.extractTextAttribute(
                        null,
                        "email"
                );

        assertThat(
                result
        ).isNull();
    }

    @Test
    void shouldReturnNullForNullAttributeName() {
        String result =
                service.extractTextAttribute(
                        oauth2User,
                        null
                );

        assertThat(
                result
        ).isNull();
    }

    @Test
    void shouldReturnNullForBlankAttributeName() {
        String result =
                service.extractTextAttribute(
                        oauth2User,
                        " "
                );

        assertThat(
                result
        ).isNull();
    }
}