package com.evomanex.AppRH.service;

import com.evomanex.AppRH.domain.user.OAuthCodeExchangeResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.TokenService;
import com.evomanex.AppRH.infra.security.mfa.MfaChallengeService;
import com.evomanex.AppRH.repository.UserMfaRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationCompletionService {

    private final TokenService tokenService;

    private final UserMfaRepository
            userMfaRepository;

    private final MfaChallengeService
            mfaChallengeService;

    public AuthenticationCompletionService(
            TokenService tokenService,
            UserMfaRepository userMfaRepository,
            MfaChallengeService mfaChallengeService
    ) {
        this.tokenService =
                tokenService;

        this.userMfaRepository =
                userMfaRepository;

        this.mfaChallengeService =
                mfaChallengeService;
    }

    public OAuthCodeExchangeResponseDTO complete(
            User user
    ) {
        if (!user.isEnabled()) {
            throw new IllegalStateException(
                    "A conta AppRH não está ativa."
            );
        }

        boolean mfaEnabled =
                userMfaRepository
                        .existsByUser_IdAndEnabledTrue(
                                user.getId()
                        );

        if (mfaEnabled) {
            String challengeToken =
                    mfaChallengeService
                            .createChallenge(
                                    user
                            );

            return new OAuthCodeExchangeResponseDTO(
                    true,
                    challengeToken,
                    null,
                    null,
                    null
            );
        }

        String accessToken =
                tokenService
                        .generateToken(
                                user
                        );

        return new OAuthCodeExchangeResponseDTO(
                false,
                null,
                accessToken,
                "Bearer",
                tokenService
                        .getExpirationSeconds()
        );
    }
}