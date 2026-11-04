package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.OAuthCodeExchangeRequestDTO;
import com.evomanex.AppRH.domain.user.OAuthCodeExchangeResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.service.AuthenticationCompletionService;
import com.evomanex.AppRH.service.OAuthLoginCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/auth/oauth"
)
public class OAuthExchangeController {

    private final OAuthLoginCodeService
            loginCodeService;

    private final AuthenticationCompletionService
            authenticationCompletionService;

    public OAuthExchangeController(
            OAuthLoginCodeService loginCodeService,
            AuthenticationCompletionService
                    authenticationCompletionService
    ) {
        this.loginCodeService =
                loginCodeService;

        this.authenticationCompletionService =
                authenticationCompletionService;
    }

    @PostMapping(
            "/exchange"
    )
    public ResponseEntity<
            OAuthCodeExchangeResponseDTO
            > exchange(
            @Valid
            @RequestBody
            OAuthCodeExchangeRequestDTO request
    ) {
        User user =
                loginCodeService
                        .consumeCode(
                                request.code()
                        );

        OAuthCodeExchangeResponseDTO response =
                authenticationCompletionService
                        .complete(
                                user
                        );

        return ResponseEntity.ok(
                response
        );
    }
}
