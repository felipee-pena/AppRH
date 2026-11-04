package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.MfaVerificationRequestDTO;
import com.evomanex.AppRH.domain.user.OAuthCodeExchangeResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.TokenService;
import com.evomanex.AppRH.infra.security.mfa.MfaChallengeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/auth/mfa"
)
public class MfaController {

    private final MfaChallengeService
            mfaChallengeService;

    private final TokenService tokenService;

    public MfaController(
            MfaChallengeService mfaChallengeService,
            TokenService tokenService
    ) {
        this.mfaChallengeService =
                mfaChallengeService;

        this.tokenService =
                tokenService;
    }

    @PostMapping(
            "/verify"
    )
    public ResponseEntity<
            OAuthCodeExchangeResponseDTO
            > verify(
            @Valid
            @RequestBody
            MfaVerificationRequestDTO request
    ) {
        User user =
                mfaChallengeService
                        .verifyTotp(
                                request.challengeToken(),
                                request.code()
                        );

        String accessToken =
                tokenService
                        .generateToken(
                                user
                        );

        OAuthCodeExchangeResponseDTO response =
                new OAuthCodeExchangeResponseDTO(
                        false,
                        null,
                        accessToken,
                        "Bearer",
                        tokenService
                                .getExpirationSeconds()
                );

        return ResponseEntity.ok(
                response
        );
    }
}