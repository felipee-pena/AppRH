package com.evomanex.AppRH.service;

import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void shouldCreateAndSendVerificationEmail()
            throws Exception {
        MimeMessage message =
                createMimeMessage();

        when(
                mailSender.createMimeMessage()
        ).thenReturn(
                message
        );

        EmailService service =
                createService(
                        ""
                );

        service.sendEmailVerification(
                "usuario@example.com",
                "token-123"
        );

        message.saveChanges();

        verify(
                mailSender
        ).send(
                message
        );

        assertThat(
                message.getSubject()
        ).isEqualTo(
                "Confirme seu cadastro no AppRH"
        );

        assertThat(
                message.getFrom()
        )
                .extracting(
                        address ->
                                ((InternetAddress) address)
                                        .getAddress()
                )
                .containsExactly(
                        "no-reply@apprh.test"
                );

        assertThat(
                message.getAllRecipients()
        )
                .extracting(
                        address ->
                                ((InternetAddress) address)
                                        .getAddress()
                )
                .containsExactly(
                        "usuario@example.com"
                );

        assertThat(
                message.isMimeType(
                        "multipart/*"
                )
        ).isTrue();

        String textContent =
                extractContentByMimeType(
                        message,
                        "text/plain"
                );

        String htmlContent =
                extractContentByMimeType(
                        message,
                        "text/html"
                );

        assertThat(
                textContent
        ).contains(
                "Seu cadastro no AppRH foi realizado com sucesso"
        );

        assertThat(
                htmlContent
        ).contains(
                "Confirme seu endereço de e-mail"
        );
    }

    @Test
    void shouldRedirectEmailToTestRecipient()
            throws Exception {
        MimeMessage message =
                createMimeMessage();

        when(
                mailSender.createMimeMessage()
        ).thenReturn(
                message
        );

        EmailService service =
                createService(
                        "  test-recipient@example.com  "
                );

        service.sendEmailVerification(
                "usuario-real@example.com",
                "token-123"
        );

        message.saveChanges();

        assertThat(
                message.getAllRecipients()
        )
                .extracting(
                        address ->
                                ((InternetAddress) address)
                                        .getAddress()
                )
                .containsExactly(
                        "test-recipient@example.com"
                );

        String textContent =
                extractContentByMimeType(
                        message,
                        "text/plain"
                );

        String htmlContent =
                extractContentByMimeType(
                        message,
                        "text/html"
                );

        assertThat(
                textContent
        ).contains(
                "usuario-real@example.com"
        );

        assertThat(
                textContent
        ).containsIgnoringCase(
                "ambiente de teste"
        );

        assertThat(
                htmlContent
        ).contains(
                "usuario-real@example.com"
        );

        assertThat(
                htmlContent
        ).containsIgnoringCase(
                "ambiente de teste"
        );
    }

    @Test
    void shouldIncludeConfirmationUrlAndToken()
            throws Exception {
        MimeMessage message =
                createMimeMessage();

        when(
                mailSender.createMimeMessage()
        ).thenReturn(
                message
        );

        EmailService service =
                new EmailService(
                        mailSender,
                        "no-reply@apprh.test",
                        "https://frontend.apprh.test",
                        "",
                        "localhost",
                        2525
                );

        service.sendEmailVerification(
                "usuario@example.com",
                "token-seguro"
        );

        message.saveChanges();

        String textContent =
                extractContentByMimeType(
                        message,
                        "text/plain"
                );

        String htmlContent =
                extractContentByMimeType(
                        message,
                        "text/html"
                );

        assertThat(
                textContent
        ).contains(
                "https://frontend.apprh.test/"
                        + "confirm-email?token=token-seguro"
        );

        assertThat(
                htmlContent
        ).contains(
                "https://frontend.apprh.test/"
                        + "confirm-email?token=token-seguro"
        );
    }

    @Test
    void shouldEscapeHtmlInsideTestNotice()
            throws Exception {
        MimeMessage message =
                createMimeMessage();

        when(
                mailSender.createMimeMessage()
        ).thenReturn(
                message
        );

        EmailService service =
                createService(
                        "test-recipient@example.com"
                );

        service.sendEmailVerification(
                "usuario+<script>@example.com",
                "token&valor"
        );

        message.saveChanges();

        String htmlContent =
                extractContentByMimeType(
                        message,
                        "text/html"
                );

        /*
         * A entrada original não pode aparecer como HTML bruto,
         * pois poderia ser interpretada como uma tag pelo cliente.
         */
        assertThat(
                htmlContent
        ).doesNotContain(
                "usuario+<script>@example.com"
        );

        /*
         * Os sinais menor e maior precisam estar escapados.
         */
        assertThat(
                htmlContent
        ).contains(
                "usuario+&lt;script&gt;@example.com"
        );

        /*
         * O "&" presente no token precisa aparecer escapado
         * dentro do atributo href do HTML.
         */
        assertThat(
                htmlContent
        ).contains(
                "token&amp;valor"
        );

        assertThat(
                htmlContent
        ).containsIgnoringCase(
                "ambiente de teste"
        );
    }

    @Test
    void shouldPropagateMailRuntimeException() {
        MimeMessage message =
                createMimeMessage();

        when(
                mailSender.createMimeMessage()
        ).thenReturn(
                message
        );

        MailSendException expectedException =
                new MailSendException(
                        "Falha SMTP"
                );

        doThrow(
                expectedException
        )
                .when(
                        mailSender
                )
                .send(
                        message
                );

        EmailService service =
                createService(
                        ""
                );

        assertThatThrownBy(
                () ->
                        service.sendEmailVerification(
                                "usuario@example.com",
                                "token-123"
                        )
        ).isSameAs(
                expectedException
        );

        verify(
                mailSender
        ).send(
                message
        );
    }

    private EmailService createService(
            String testRecipient
    ) {
        return new EmailService(
                mailSender,
                "no-reply@apprh.test",
                "http://localhost:5173",
                testRecipient,
                "localhost",
                2525
        );
    }

    private static MimeMessage createMimeMessage() {
        Session session =
                Session.getInstance(
                        new Properties()
                );

        return new MimeMessage(
                session
        );
    }

    private static String extractContentByMimeType(
            Part part,
            String expectedMimeType
    ) throws Exception {
        if (
                part.isMimeType(
                        expectedMimeType
                )
        ) {
            Object content =
                    part.getContent();

            return content == null
                    ? ""
                    : content.toString();
        }

        if (
                part.isMimeType(
                        "multipart/*"
                )
        ) {
            Multipart multipart =
                    (Multipart) part.getContent();

            for (
                    int index = 0;
                    index < multipart.getCount();
                    index++
            ) {
                BodyPart bodyPart =
                        multipart.getBodyPart(
                                index
                        );

                String extractedContent =
                        extractContentByMimeType(
                                bodyPart,
                                expectedMimeType
                        );

                if (
                        extractedContent != null
                                && !extractedContent.isBlank()
                ) {
                    return extractedContent;
                }
            }
        }

        return "";
    }
}