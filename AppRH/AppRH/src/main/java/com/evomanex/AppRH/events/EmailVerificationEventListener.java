package com.evomanex.AppRH.events;

import com.evomanex.AppRH.service.EmailService;

import org.springframework.stereotype.Component;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailVerificationEventListener {

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
        emailService.sendEmailVerification(
                event.destinationEmail(),
                event.rawToken()
        );
    }
}