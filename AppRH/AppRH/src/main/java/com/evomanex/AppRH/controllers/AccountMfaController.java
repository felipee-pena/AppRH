package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.MessageResponseDTO;
import com.evomanex.AppRH.domain.user.MfaConfirmRequestDTO;
import com.evomanex.AppRH.domain.user.MfaDisableRequestDTO;
import com.evomanex.AppRH.domain.user.MfaSetupResponseDTO;
import com.evomanex.AppRH.domain.user.MfaStatusResponseDTO;
import com.evomanex.AppRH.domain.user.RecoveryCodesResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.mfa.MfaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        "/api/account/mfa"
)
public class AccountMfaController {

    private final MfaService mfaService;

    public AccountMfaController(
            MfaService mfaService
    ) {
        this.mfaService =
                mfaService;
    }

    @GetMapping(
            "/status"
    )
    public ResponseEntity<MfaStatusResponseDTO>
    status(
            @AuthenticationPrincipal
            User user
    ) {
        validateAuthenticatedUser(
                user
        );

        boolean enabled =
                mfaService.isMfaEnabled(
                        user
                );

        MfaStatusResponseDTO response =
                new MfaStatusResponseDTO(
                        enabled
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping(
            "/totp/setup"
    )
    public ResponseEntity<MfaSetupResponseDTO>
    beginTotpSetup(
            @AuthenticationPrincipal
            User user
    ) {
        validateAuthenticatedUser(
                user
        );

        MfaSetupResponseDTO response =
                mfaService.beginSetup(
                        user
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping(
            "/totp/confirm"
    )
    public ResponseEntity<MessageResponseDTO>
    confirmTotpSetup(
            @AuthenticationPrincipal
            User user,
            @Valid
            @RequestBody
            MfaConfirmRequestDTO request
    ) {
        validateAuthenticatedUser(
                user
        );

        mfaService.confirmSetup(
                user,
                request.code()
        );

        MessageResponseDTO response =
                new MessageResponseDTO(
                        "A autenticação em dois fatores "
                                + "foi habilitada com sucesso."
                );

        return ResponseEntity.ok(
                response
        );
    }

    @DeleteMapping(
            "/totp"
    )
    public ResponseEntity<MessageResponseDTO>
    disableTotp(
            @AuthenticationPrincipal
            User user,
            @Valid
            @RequestBody
            MfaDisableRequestDTO request
    ) {
        validateAuthenticatedUser(
                user
        );

        mfaService.disable(
                user,
                request.password(),
                request.code()
        );

        MessageResponseDTO response =
                new MessageResponseDTO(
                        "A autenticação em dois fatores "
                                + "foi desabilitada com sucesso."
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping(
            "/recovery-codes/regenerate"
    )
    public ResponseEntity<RecoveryCodesResponseDTO>
    regenerateRecoveryCodes(
            @AuthenticationPrincipal
            User user
    ) {
        validateAuthenticatedUser(
                user
        );

        List<String> recoveryCodes =
                mfaService.regenerateRecoveryCodes(
                        user
                );

        RecoveryCodesResponseDTO response =
                new RecoveryCodesResponseDTO(
                        recoveryCodes
                );

        return ResponseEntity.ok(
                response
        );
    }

    private void validateAuthenticatedUser(
            User user
    ) {
        if (user == null) {
            throw new IllegalStateException(
                    "O usuário autenticado não foi encontrado."
            );
        }
    }
}