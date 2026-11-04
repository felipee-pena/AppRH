package com.evomanex.AppRH.infra.security.oauth;

import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.repository.UserRepository;
import com.evomanex.AppRH.service.OAuthLoginCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuthAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private final OAuthLoginCodeService
            loginCodeService;

    private final String frontendUrl;

    public OAuthAuthenticationSuccessHandler(
            UserRepository userRepository,
            OAuthLoginCodeService loginCodeService,
            @Value(
                    "${app.frontend-url}"
            )
            String frontendUrl
    ) {
        this.userRepository =
                userRepository;

        this.loginCodeService =
                loginCodeService;

        this.frontendUrl =
                frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        String userId =
                extractAppRhUserId(
                        authentication
                );

        User user =
                userRepository
                        .findById(
                                userId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Usuário AppRH não encontrado."
                                        )
                        );

        if (!user.isEnabled()) {
            throw new IllegalStateException(
                    "A conta AppRH não está ativa."
            );
        }

        String code =
                loginCodeService
                        .createCode(
                                user
                        );

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(
                                frontendUrl
                        )
                        .path(
                                "/oauth/callback"
                        )
                        .queryParam(
                                "code",
                                code
                        )
                        .build()
                        .encode()
                        .toUriString();

        response.sendRedirect(
                redirectUrl
        );
    }

    private String extractAppRhUserId(
            Authentication authentication
    ) {
        Object principal =
                authentication.getPrincipal();

        if (
                principal
                        instanceof OAuth2User oauth2User
        ) {
            Object id =
                    oauth2User.getAttribute(
                            "apprhUserId"
                    );

            if (id == null) {
                throw new IllegalStateException(
                        "O usuário OAuth não possui "
                                + "identificador AppRH."
                );
            }

            return String.valueOf(
                    id
            );
        }

        throw new IllegalStateException(
                "Usuário OAuth inválido."
        );
    }
}