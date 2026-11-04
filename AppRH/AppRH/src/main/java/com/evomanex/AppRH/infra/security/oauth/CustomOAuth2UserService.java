package com.evomanex.AppRH.infra.security.oauth;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.service.SocialLoginService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final SocialLoginService socialLoginService;

    private final GitHubEmailService githubEmailService;

    public CustomOAuth2UserService(
            SocialLoginService socialLoginService,
            GitHubEmailService githubEmailService
    ) {
        this.socialLoginService =
                socialLoginService;

        this.githubEmailService =
                githubEmailService;
    }

    @Override
    public OAuth2User loadUser(
            OAuth2UserRequest userRequest
    ) throws OAuth2AuthenticationException {
        OAuth2User oauth2User =
                super.loadUser(
                        userRequest
                );

        String registrationId =
                userRequest
                        .getClientRegistration()
                        .getRegistrationId();

        ExternalIdentityProvider provider =
                ExternalIdentityProvider
                        .fromRegistrationId(
                                registrationId
                        );

        String providerUserId =
                extractProviderUserId(
                        oauth2User
                );

        String email =
                extractTextAttribute(
                        oauth2User,
                        "email"
                );

        if (
                provider
                        == ExternalIdentityProvider.GITHUB
                        && email == null
        ) {
            email =
                    githubEmailService
                            .findPrimaryVerifiedEmail(
                                    userRequest
                                            .getAccessToken()
                                            .getTokenValue()
                            );
        }

        String username =
                extractTextAttribute(
                        oauth2User,
                        "login"
                );

        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        provider,
                        providerUserId,
                        email,
                        email != null
                                && !email.isBlank(),
                        username
                );

        User user =
                socialLoginService.resolveUser(
                        profile
                );

        Map<String, Object> attributes =
                new HashMap<>(
                        oauth2User.getAttributes()
                );

        attributes.put(
                "apprhUserId",
                user.getId()
        );

        return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                attributes,
                "login"
        );
    }

    public String extractProviderUserId(
            OAuth2User oauth2User
    ) {
        if (oauth2User == null) {
            throw authenticationException(
                    "invalid_oauth_user",
                    "O usuário OAuth é obrigatório."
            );
        }

        Object providerUserIdAttribute =
                oauth2User.getAttribute(
                        "id"
                );

        if (providerUserIdAttribute == null) {
            throw authenticationException(
                    "missing_provider_user_id",
                    "O GitHub não informou "
                            + "o identificador do usuário."
            );
        }

        String providerUserId =
                providerUserIdAttribute
                        .toString()
                        .trim();

        if (providerUserId.isBlank()) {
            throw authenticationException(
                    "missing_provider_user_id",
                    "O GitHub não informou "
                            + "o identificador do usuário."
            );
        }

        return providerUserId;
    }

    public String extractTextAttribute(
            OAuth2User oauth2User,
            String attributeName
    ) {
        if (
                oauth2User == null
                        || attributeName == null
                        || attributeName.isBlank()
        ) {
            return null;
        }

        Object attribute =
                oauth2User.getAttribute(
                        attributeName
                );

        if (attribute == null) {
            return null;
        }

        String value =
                attribute
                        .toString()
                        .trim();

        return value.isBlank()
                ? null
                : value;
    }

    private OAuth2AuthenticationException
    authenticationException(
            String code,
            String description
    ) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(
                        code,
                        description,
                        null
                ),
                description
        );
    }
}