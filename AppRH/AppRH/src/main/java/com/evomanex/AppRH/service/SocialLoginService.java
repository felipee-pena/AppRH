package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.identity.UserExternalIdentity;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.oauth.OAuthAccountLinkException;
import com.evomanex.AppRH.infra.security.oauth.OAuthProviderProfile;
import com.evomanex.AppRH.repository.UserExternalIdentityRepository;
import com.evomanex.AppRH.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class SocialLoginService {

    private final UserRepository userRepository;

    private final UserExternalIdentityRepository
            externalIdentityRepository;

    public SocialLoginService(
            UserRepository userRepository,
            UserExternalIdentityRepository
                    externalIdentityRepository
    ) {
        this.userRepository =
                userRepository;

        this.externalIdentityRepository =
                externalIdentityRepository;
    }

    @Transactional
    public User resolveUser(
            OAuthProviderProfile profile
    ) {
        validateProfile(
                profile
        );

        return externalIdentityRepository
                .findByProviderAndProviderUserId(
                        profile.provider(),
                        profile.providerUserId()
                )
                .map(
                        identity ->
                                updateExistingIdentity(
                                        identity,
                                        profile
                                )
                )
                .orElseGet(
                        () ->
                                linkExistingUser(
                                        profile
                                )
                );
    }

    private User updateExistingIdentity(
            UserExternalIdentity identity,
            OAuthProviderProfile profile
    ) {
        User user =
                identity.getUser();

        validateExistingUser(
                user
        );

        identity.registerLogin(
                normalizeEmail(
                        profile.email()
                ),
                profile.username(),
                profile.emailVerified()
        );

        return user;
    }

    private User linkExistingUser(
            OAuthProviderProfile profile
    ) {
        String normalizedEmail =
                normalizeEmail(
                        profile.email()
                );

        if (
                normalizedEmail == null
                        || normalizedEmail.isBlank()
        ) {
            throw new OAuthAccountLinkException(
                    "O provedor não disponibilizou um e-mail."
            );
        }

        if (!profile.emailVerified()) {
            throw new OAuthAccountLinkException(
                    "O e-mail informado pelo provedor "
                            + "não está verificado."
            );
        }

        User user =
                userRepository
                        .findByLoginIgnoreCase(
                                normalizedEmail
                        )
                        .orElseThrow(
                                () ->
                                        new OAuthAccountLinkException(
                                                "Não existe uma conta AppRH "
                                                        + "com esse e-mail."
                                        )
                        );

        validateExistingUser(
                user
        );

        externalIdentityRepository
                .findByUser_IdAndProvider(
                        user.getId(),
                        profile.provider()
                )
                .ifPresent(
                        existing -> {
                            throw new OAuthAccountLinkException(
                                    "A conta AppRH já está vinculada "
                                            + "a outra identidade "
                                            + "desse provedor."
                            );
                        }
                );

        UserExternalIdentity identity =
                new UserExternalIdentity(
                        user,
                        profile.provider(),
                        profile.providerUserId(),
                        normalizedEmail,
                        profile.username(),
                        profile.emailVerified()
                );

        externalIdentityRepository.save(
                identity
        );

        return user;
    }

    private void validateProfile(
            OAuthProviderProfile profile
    ) {
        if (profile == null) {
            throw new OAuthAccountLinkException(
                    "O perfil OAuth é obrigatório."
            );
        }

        if (profile.provider() == null) {
            throw new OAuthAccountLinkException(
                    "O provedor OAuth é obrigatório."
            );
        }

        if (
                profile.providerUserId() == null
                        || profile.providerUserId().isBlank()
        ) {
            throw new OAuthAccountLinkException(
                    "O provedor não informou "
                            + "o identificador do usuário."
            );
        }
    }

    private void validateExistingUser(
            User user
    ) {
        if (user == null) {
            throw new OAuthAccountLinkException(
                    "O usuário AppRH não foi encontrado."
            );
        }

        if (!user.isEnabled()) {
            throw new OAuthAccountLinkException(
                    "A conta AppRH não está ativa."
            );
        }

        if (!user.isAccountNonLocked()) {
            throw new OAuthAccountLinkException(
                    "A conta AppRH está bloqueada."
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {
        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }
}