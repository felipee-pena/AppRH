package com.evomanex.AppRH.events;

import com.evomanex.AppRH.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailVerificationEventListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmailVerificationEventListener.class
            );

    private final EmailService emailService;

    public EmailVerificationEventListener(
            EmailService emailService
    ) {
        this.emailService =
                emailService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            EmailVerificationRequestedEvent event
    ) {
        LOGGER.info(
                "Iniciando envio de confirmação após commit"
        );

        try {
            emailService.sendEmailVerification(
                    event.destinationEmail(),
                    event.rawToken()
            );

            LOGGER.info(
                    "Solicitação de e-mail enviada ao servidor SMTP"
            );
        }
        catch (RuntimeException exception) {
            LOGGER.error(
                    "Falha no envio do e-mail de confirmação após commit",
                    exception
            );
        }
    }
}