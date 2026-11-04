package com.evomanex.AppRH.infra.security.oauth;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.service.SocialLoginService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOidcUserService
        extends OidcUserService {

    private final SocialLoginService
            socialLoginService;

    public CustomOidcUserService(
            SocialLoginService socialLoginService
    ) {
        this.socialLoginService =
                socialLoginService;
    }

    @Override
    public OidcUser loadUser(
            OidcUserRequest userRequest
    ) throws OAuth2AuthenticationException {
        OidcUser oidcUser =
                super.loadUser(
                        userRequest
                );

        OAuthProviderProfile profile =
                new OAuthProviderProfile(
                        ExternalIdentityProvider.GOOGLE,
                        oidcUser.getSubject(),
                        oidcUser.getEmail(),
                        Boolean.TRUE.equals(
                                oidcUser.getEmailVerified()
                        ),
                        oidcUser.getFullName()
                );

        User user =
                socialLoginService.resolveUser(
                        profile
                );

        Map<String, Object> claims =
                new HashMap<>(
                        oidcUser.getClaims()
                );

        claims.put(
                "apprhUserId",
                user.getId()
        );

        OidcUserInfo userInfo =
                new OidcUserInfo(
                        claims
                );

        return new DefaultOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                userInfo,
                "sub"
        );
    }
}