package com.evomanex.AppRH.controllers;

import com.evomanex.AppRH.domain.user.MessageResponseDTO;
import com.evomanex.AppRH.domain.user.MfaConfirmRequestDTO;
import com.evomanex.AppRH.domain.user.MfaDisableRequestDTO;
import com.evomanex.AppRH.domain.user.MfaSetupResponseDTO;
import com.evomanex.AppRH.domain.user.MfaStatusResponseDTO;
import com.evomanex.AppRH.domain.user.RecoveryCodesResponseDTO;
import com.evomanex.AppRH.domain.user.User;
import com.evomanex.AppRH.infra.security.mfa.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountMfaControllerTest {

    @Mock
    private MfaService mfaService;

    @Mock
    private User user;

    private AccountMfaController controller;

    @BeforeEach
    void setUp() {
        controller =
                new AccountMfaController(
                        mfaService
                );
    }

    @Test
    void shouldReturnEnabledMfaStatus() {
        when(
                mfaService.isMfaEnabled(
                        user
                )
        ).thenReturn(
                true
        );

        ResponseEntity<MfaStatusResponseDTO> result =
                controller.status(
                        user
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().mfaEnabled()
        ).isTrue();

        verify(
                mfaService
        ).isMfaEnabled(
                user
        );
    }

    @Test
    void shouldReturnDisabledMfaStatus() {
        when(
                mfaService.isMfaEnabled(
                        user
                )
        ).thenReturn(
                false
        );

        ResponseEntity<MfaStatusResponseDTO> result =
                controller.status(
                        user
                );

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().mfaEnabled()
        ).isFalse();
    }

    @Test
    void shouldBeginTotpSetup() {
        MfaSetupResponseDTO expected =
                new MfaSetupResponseDTO(
                        "BASE32SECRET",
                        "otpauth://totp/AppRH",
                        List.of(
                                "CODE01",
                                "CODE02"
                        )
                );

        when(
                mfaService.beginSetup(
                        user
                )
        ).thenReturn(
                expected
        );

        ResponseEntity<MfaSetupResponseDTO> result =
                controller.beginTotpSetup(
                        user
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isSameAs(
                expected
        );

        assertThat(
                result.getBody()
                        .recoveryCodes()
        ).containsExactly(
                "CODE01",
                "CODE02"
        );

        verify(
                mfaService
        ).beginSetup(
                user
        );
    }

    @Test
    void shouldConfirmTotpSetup() {
        MfaConfirmRequestDTO request =
                new MfaConfirmRequestDTO(
                        "123456"
                );

        ResponseEntity<MessageResponseDTO> result =
                controller.confirmTotpSetup(
                        user,
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().message()
        ).isEqualTo(
                "A autenticação em dois fatores "
                        + "foi habilitada com sucesso."
        );

        verify(
                mfaService
        ).confirmSetup(
                user,
                "123456"
        );
    }

    @Test
    void shouldDisableTotp() {
        MfaDisableRequestDTO request =
                new MfaDisableRequestDTO(
                        "current-password",
                        "123456"
                );

        ResponseEntity<MessageResponseDTO> result =
                controller.disableTotp(
                        user,
                        request
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().message()
        ).isEqualTo(
                "A autenticação em dois fatores "
                        + "foi desabilitada com sucesso."
        );

        verify(
                mfaService
        ).disable(
                user,
                "current-password",
                "123456"
        );
    }

    @Test
    void shouldRegenerateRecoveryCodes() {
        when(
                mfaService.regenerateRecoveryCodes(
                        user
                )
        ).thenReturn(
                List.of(
                        "RECOVERY01",
                        "RECOVERY02",
                        "RECOVERY03"
                )
        );

        ResponseEntity<RecoveryCodesResponseDTO> result =
                controller.regenerateRecoveryCodes(
                        user
                );

        assertThat(
                result.getStatusCode().is2xxSuccessful()
        ).isTrue();

        assertThat(
                result.getBody()
        ).isNotNull();

        assertThat(
                result.getBody().recoveryCodes()
        ).containsExactly(
                "RECOVERY01",
                "RECOVERY02",
                "RECOVERY03"
        );

        verify(
                mfaService
        ).regenerateRecoveryCodes(
                user
        );
    }

    @Test
    void shouldRejectMissingAuthenticatedUserForStatus() {
        assertThatThrownBy(
                () ->
                        controller.status(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O usuário autenticado não foi encontrado."
                );
    }

    @Test
    void shouldRejectMissingAuthenticatedUserForSetup() {
        assertThatThrownBy(
                () ->
                        controller.beginTotpSetup(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O usuário autenticado não foi encontrado."
                );
    }

    @Test
    void shouldRejectMissingAuthenticatedUserForConfirmation() {
        MfaConfirmRequestDTO request =
                new MfaConfirmRequestDTO(
                        "123456"
                );

        assertThatThrownBy(
                () ->
                        controller.confirmTotpSetup(
                                null,
                                request
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O usuário autenticado não foi encontrado."
                );
    }

    @Test
    void shouldRejectMissingAuthenticatedUserForDisable() {
        MfaDisableRequestDTO request =
                new MfaDisableRequestDTO(
                        "current-password",
                        "123456"
                );

        assertThatThrownBy(
                () ->
                        controller.disableTotp(
                                null,
                                request
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O usuário autenticado não foi encontrado."
                );
    }

    @Test
    void shouldRejectMissingAuthenticatedUserForRecoveryRegeneration() {
        assertThatThrownBy(
                () ->
                        controller.regenerateRecoveryCodes(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "O usuário autenticado não foi encontrado."
                );
    }
}