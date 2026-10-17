package com.evomanex.AppRH.service;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final String sender;
    private final String frontendUrl;
    private final String testRecipient;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}")
            String sender,
            @Value("${app.frontend.url}")
            String frontendUrl,
            @Value("${app.mail.test-recipient:}")
            String testRecipient
    ) {
        this.mailSender = mailSender;
        this.sender = sender;
        this.frontendUrl = frontendUrl;
        this.testRecipient = testRecipient;
    }

    public void sendEmailVerification(
            String originalRecipient,
            String rawToken
    ) {
        String effectiveRecipient =
                resolveRecipient(
                        originalRecipient
                );

        String confirmationUrl =
                frontendUrl
                        + "/confirm-email?token="
                        + rawToken;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(sender);
        message.setTo(effectiveRecipient);
        message.setSubject(
                "Confirme seu cadastro no AppRH"
        );

        message.setText(
                buildMessage(
                        originalRecipient,
                        confirmationUrl
                )
        );

        mailSender.send(message);
    }

    private String resolveRecipient(
            String originalRecipient
    ) {
        if (
                testRecipient != null
                        && !testRecipient.isBlank()
        ) {
            return testRecipient.trim();
        }

        return originalRecipient;
    }

    private String buildMessage(
            String originalRecipient,
            String confirmationUrl
    ) {
        StringBuilder body =
                new StringBuilder();

        body.append(
                """
                Olá!

                Seu cadastro no AppRH foi realizado.

                Para confirmar o seu endereço de e-mail, acesse o link:

                """
        );

        body.append(confirmationUrl);

        body.append(
                """



                O link é válido por 24 horas e poderá ser utilizado uma única vez.

                Se você não realizou este cadastro, desconsidere esta mensagem.
                """
        );

        if (
                testRecipient != null
                        && !testRecipient.isBlank()
        ) {
            body.append(
                    """



                    Ambiente de teste.
                    Destinatário original:
                    """
            );

            body.append(originalRecipient);
        }

        return body.toString();
    }
}