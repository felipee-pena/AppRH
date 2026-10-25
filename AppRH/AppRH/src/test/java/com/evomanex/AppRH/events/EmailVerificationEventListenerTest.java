package com.evomanex.AppRH.events;

import com.evomanex.AppRH.service.EmailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationEventListener listener;

    @Test
    void shouldSendVerificationEmailAfterEvent() {
        EmailVerificationRequestedEvent event =
                new EmailVerificationRequestedEvent(
                        "usuario@example.com",
                        "token-bruto"
                );

        listener.handle(
                event
        );

        verify(
                emailService
        ).sendEmailVerification(
                "usuario@example.com",
                "token-bruto"
        );
    }

    @Test
    void shouldNotPropagateRuntimeExceptionFromEmailService() {
        EmailVerificationRequestedEvent event =
                new EmailVerificationRequestedEvent(
                        "usuario@example.com",
                        "token-bruto"
                );

        doThrow(
                new IllegalStateException(
                        "Falha SMTP"
                )
        )
                .when(
                        emailService
                )
                .sendEmailVerification(
                        "usuario@example.com",
                        "token-bruto"
                );

        assertThatCode(
                () ->
                        listener.handle(
                                event
                        )
        ).doesNotThrowAnyException();

        verify(
                emailService
        ).sendEmailVerification(
                "usuario@example.com",
                "token-bruto"
        );
    }
}