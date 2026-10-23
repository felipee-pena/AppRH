package com.evomanex.AppRH.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {


    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EmailService.class
            );

    private final JavaMailSender mailSender;

    private final String sender;
    private final String frontendUrl;
    private final String testRecipient;
    private final String mailHost;
    private final int mailPort;

    public EmailService(
            JavaMailSender mailSender,

            @Value("${app.mail.from}")
            String sender,

            @Value("${app.frontend.url}")
            String frontendUrl,

            @Value("${app.mail.test-recipient:}")
            String testRecipient,

            @Value("${spring.mail.host}")
            String mailHost,

            @Value("${spring.mail.port:587}")
            int mailPort
    ) {
        this.mailSender =
                mailSender;

        this.sender =
                sender;

        this.frontendUrl =
                frontendUrl;

        this.testRecipient =
                testRecipient;

        this.mailHost =
                mailHost;

        this.mailPort =
                mailPort;
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

        LOGGER.info(
                "Preparando e-mail. Host SMTP: {}. Porta: {}. "
                        + "Remetente: {}. Destinatário efetivo: {}. "
                        + "Destinatário original: {}",
                mailHost,
                mailPort,
                sender,
                effectiveRecipient,
                originalRecipient
        );

        MimeMessage message =
                mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(
                    sender
            );

            helper.setTo(
                    effectiveRecipient
            );

            helper.setSubject(
                    "Confirme seu cadastro no AppRH"
            );

            helper.setText(
                    buildTextMessage(
                            originalRecipient,
                            confirmationUrl
                    ),
                    buildHtmlMessage(
                            originalRecipient,
                            confirmationUrl
                    )
            );

            LOGGER.info(
                    "Iniciando envio SMTP para {}",
                    effectiveRecipient
            );

            mailSender.send(
                    message
            );

            LOGGER.info(
                    "JavaMailSender concluiu o envio para {}",
                    effectiveRecipient
            );
        }
        catch (MessagingException exception) {
            LOGGER.error(
                    "Falha ao preparar o e-mail para {}",
                    effectiveRecipient,
                    exception
            );

            throw new IllegalStateException(
                    "Não foi possível preparar o e-mail de confirmação",
                    exception
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Falha inesperada ao enviar o e-mail para {}",
                    effectiveRecipient,
                    exception
            );

            throw exception;
        }

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

    private String buildTextMessage(
            String originalRecipient,
            String confirmationUrl
    ) {
        return "Olá!\n\n"
                + "Seu cadastro no AppRH foi realizado com sucesso.\n\n"
                + "Para confirmar seu endereço de e-mail, acesse:\n\n"
                + confirmationUrl
                + "\n\n"
                + "Este link é válido por 24 horas e pode ser utilizado "
                + "apenas uma vez.\n\n"
                + "Se você não realizou este cadastro, "
                + "desconsidere esta mensagem.\n\n"
                + "AppRH\n"
                + "Conectando profissionais e oportunidades."
                + buildTextTestNotice(
                originalRecipient
        );
    }

    private String buildHtmlMessage(
            String originalRecipient,
            String confirmationUrl
    ) {
        String safeConfirmationUrl =
                escapeHtml(
                        confirmationUrl
                );

        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Confirme seu cadastro no AppRH</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f4f6f8;
                             font-family:Arial,sans-serif;color:#1f2937;">
                    <table role="presentation"
                           width="100%%"
                           cellspacing="0"
                           cellpadding="0"
                           style="background-color:#f4f6f8;
                                  padding:32px 16px;">
                        <tr>
                            <td align="center">
                                <table role="presentation"
                                       width="100%%"
                                       cellspacing="0"
                                       cellpadding="0"
                                       style="max-width:600px;
                                              background-color:#ffffff;
                                              border-radius:16px;
                                              overflow:hidden;
                                              box-shadow:0 8px 24px
                                              rgba(15,23,42,0.08);">
                                    <tr>
                                        <td style="background-color:#2563eb;
                                                   padding:28px 36px;
                                                   text-align:center;">
                                            <div style="font-size:28px;
                                                        font-weight:700;
                                                        color:#ffffff;">
                                                AppRH
                                            </div>
                                            <div style="margin-top:6px;
                                                        font-size:14px;
                                                        color:#dbeafe;">
                                                Conectando profissionais e oportunidades
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:36px;">
                                            <h1 style="margin:0 0 18px;
                                                       font-size:24px;
                                                       line-height:1.3;
                                                       color:#111827;">
                                                Confirme seu endereço de e-mail
                                            </h1>

                                            <p style="margin:0 0 16px;
                                                      font-size:16px;
                                                      line-height:1.6;
                                                      color:#4b5563;">
                                                Seu cadastro no AppRH foi
                                                realizado com sucesso.
                                            </p>

                                            <p style="margin:0 0 26px;
                                                      font-size:16px;
                                                      line-height:1.6;
                                                      color:#4b5563;">
                                                Clique no botão abaixo para
                                                confirmar seu endereço de e-mail.
                                            </p>

                                            <table role="presentation"
                                                   cellspacing="0"
                                                   cellpadding="0">
                                                <tr>
                                                    <td style="border-radius:10px;
                                                               background-color:#2563eb;">
                                                        %s
                                                            Confirmar meu e-mail
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>

                                            <p style="margin:26px 0 0;
                                                      font-size:14px;
                                                      line-height:1.6;
                                                      color:#6b7280;">
                                                O link é válido por 24 horas
                                                e pode ser utilizado apenas uma vez.
                                            </p>

                                            <p style="margin:12px 0 0;
                                                      font-size:14px;
                                                      line-height:1.6;
                                                      color:#6b7280;">
                                                Se você não realizou este cadastro,
                                                nenhuma ação é necessária.
                                            </p>

                                            %s
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:22px 36px;
                                                   background-color:#f8fafc;
                                                   border-top:1px solid #e5e7eb;
                                                   text-align:center;">
                                            <p style="margin:0;
                                                      font-size:12px;
                                                      line-height:1.6;
                                                      color:#94a3b8;">
                                                Esta é uma mensagem automática.
                                                Não responda a este e-mail.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(
                        safeConfirmationUrl,
                        buildHtmlTestNotice(
                                originalRecipient
                        )
                );
    }

    private String buildTextTestNotice(
            String originalRecipient
    ) {
        if (
                testRecipient == null
                        || testRecipient.isBlank()
        ) {
            return "";
        }

        return "\n\nAMBIENTE DE TESTE\n"
                + "Destinatário original: "
                + originalRecipient;
    }

    private String buildHtmlTestNotice(
            String originalRecipient
    ) {
        if (
                testRecipient == null
                        || testRecipient.isBlank()
        ) {
            return "";
        }

        return """
                <div style="margin-top:28px;
                            padding:16px;
                            border-radius:10px;
                            background-color:#fffbeb;
                            border:1px solid #fde68a;">
                    <p style="margin:0;
                              font-size:13px;
                              line-height:1.5;
                              color:#92400e;">
                        <strong>Ambiente de teste</strong><br>
                        Destinatário original: %s
                    </p>
                </div>
                """
                .formatted(
                        escapeHtml(
                                originalRecipient
                        )
                );
    }

    private String escapeHtml(
            String value
    ) {
        return value
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }
}