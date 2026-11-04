package com.evomanex.AppRH.infra.security.mfa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TotpServiceTest {

    private TotpService totpService;

    @BeforeEach
    void setUp() {
        totpService =
                new TotpService();
    }

    @Test
    void shouldGenerateBase32Secret() {
        String secret =
                totpService.generateSecret();

        assertThat(
                secret
        ).isNotNull();

        assertThat(
                secret
        ).isNotBlank();

        assertThat(
                secret
        ).matches(
                "[A-Z2-7]+"
        );
    }

    @Test
    void shouldGenerateDifferentSecrets() {
        String firstSecret =
                totpService.generateSecret();

        String secondSecret =
                totpService.generateSecret();

        assertThat(
                firstSecret
        ).isNotEqualTo(
                secondSecret
        );
    }

    @Test
    void shouldGenerateOtpAuthUri() {
        String uri =
                totpService.generateOtpAuthUri(
                        "AppRH",
                        "usuario@example.com",
                        "JBSWY3DPEHPK3PXP"
                );

        assertThat(
                uri
        ).startsWith(
                "otpauth://totp/"
        );

        assertThat(
                uri
        ).contains(
                "AppRH"
        );

        assertThat(
                uri
        ).contains(
                "usuario%40example.com"
        );

        assertThat(
                uri
        ).contains(
                "secret=JBSWY3DPEHPK3PXP"
        );

        assertThat(
                uri
        ).contains(
                "issuer=AppRH"
        );

        assertThat(
                uri
        ).contains(
                "algorithm=SHA1"
        );

        assertThat(
                uri
        ).contains(
                "digits=6"
        );

        assertThat(
                uri
        ).contains(
                "period=30"
        );
    }

    @Test
    void shouldVerifyValidCurrentCode() {
        String secretBase32 =
                "JBSWY3DPEHPK3PXP";

        byte[] secretBytes =
                decodeBase32(
                        secretBase32
                );

        long currentCounter =
                System.currentTimeMillis()
                        / 1000L
                        / 30L;

        String currentCode =
                totpService.generateCode(
                        secretBytes,
                        currentCounter
                );

        boolean valid =
                totpService.verify(
                        secretBase32,
                        currentCode
                );

        assertThat(
                valid
        ).isTrue();
    }

    @Test
    void shouldAcceptCodeFromPreviousTimeWindow() {
        String secretBase32 =
                "JBSWY3DPEHPK3PXP";

        byte[] secretBytes =
                decodeBase32(
                        secretBase32
                );

        long currentCounter =
                System.currentTimeMillis()
                        / 1000L
                        / 30L;

        String previousCode =
                totpService.generateCode(
                        secretBytes,
                        currentCounter - 1
                );

        boolean valid =
                totpService.verify(
                        secretBase32,
                        previousCode
                );

        assertThat(
                valid
        ).isTrue();
    }

    @Test
    void shouldAcceptCodeFromNextTimeWindow() {
        String secretBase32 =
                "JBSWY3DPEHPK3PXP";

        byte[] secretBytes =
                decodeBase32(
                        secretBase32
                );

        long currentCounter =
                System.currentTimeMillis()
                        / 1000L
                        / 30L;

        String nextCode =
                totpService.generateCode(
                        secretBytes,
                        currentCounter + 1
                );

        boolean valid =
                totpService.verify(
                        secretBase32,
                        nextCode
                );

        assertThat(
                valid
        ).isTrue();
    }

    @Test
    void shouldRejectInvalidCode() {
        boolean valid =
                totpService.verify(
                        "JBSWY3DPEHPK3PXP",
                        "999999"
                );

        assertThat(
                valid
        ).isFalse();
    }

    @Test
    void shouldRejectCodeWithInvalidLength() {
        boolean valid =
                totpService.verify(
                        "JBSWY3DPEHPK3PXP",
                        "12345"
                );

        assertThat(
                valid
        ).isFalse();
    }

    @Test
    void shouldRejectCodeContainingLetters() {
        boolean valid =
                totpService.verify(
                        "JBSWY3DPEHPK3PXP",
                        "12AB56"
                );

        assertThat(
                valid
        ).isFalse();
    }

    @Test
    void shouldRejectBlankSecret() {
        boolean valid =
                totpService.verify(
                        " ",
                        "123456"
                );

        assertThat(
                valid
        ).isFalse();
    }

    @Test
    void shouldRejectNullSecret() {
        boolean valid =
                totpService.verify(
                        null,
                        "123456"
                );

        assertThat(
                valid
        ).isFalse();
    }

    @Test
    void shouldRejectInvalidBase32Secret() {
        boolean valid =
                totpService.verify(
                        "INVALID-SECRET-019",
                        "123456"
                );

        assertThat(
                valid
        ).isFalse();
    }

    @Test
    void shouldRejectNullCode() {
        boolean valid =
                totpService.verify(
                        "JBSWY3DPEHPK3PXP",
                        null
                );

        assertThat(
                valid
        ).isFalse();
    }

    @Test
    void shouldRejectBlankIssuer() {
        assertThatThrownBy(
                () ->
                        totpService.generateOtpAuthUri(
                                " ",
                                "usuario@example.com",
                                "JBSWY3DPEHPK3PXP"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O nome do emissor é obrigatório."
                );
    }

    @Test
    void shouldRejectBlankAccountName() {
        assertThatThrownBy(
                () ->
                        totpService.generateOtpAuthUri(
                                "AppRH",
                                " ",
                                "JBSWY3DPEHPK3PXP"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O nome da conta é obrigatório."
                );
    }

    @Test
    void shouldRejectBlankSecretWhenGeneratingUri() {
        assertThatThrownBy(
                () ->
                        totpService.generateOtpAuthUri(
                                "AppRH",
                                "usuario@example.com",
                                " "
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O segredo TOTP é obrigatório."
                );
    }

    private byte[] decodeBase32(
            String value
    ) {
        String normalized =
                value
                        .replace(
                                "=",
                                ""
                        )
                        .replace(
                                " ",
                                ""
                        )
                        .toUpperCase(
                                Locale.ROOT
                        );

        ByteArrayOutputStream result =
                new ByteArrayOutputStream();

        int buffer =
                0;

        int bitsLeft =
                0;

        for (
                int index = 0;
                index < normalized.length();
                index++
        ) {
            char character =
                    normalized.charAt(
                            index
                    );

            int alphabetIndex =
                    getBase32Index(
                            character
                    );

            if (alphabetIndex < 0) {
                throw new IllegalArgumentException(
                        "Valor Base32 inválido."
                );
            }

            buffer =
                    buffer << 5
                            | alphabetIndex;

            bitsLeft +=
                    5;

            if (bitsLeft >= 8) {
                result.write(
                        buffer
                                >> (
                                bitsLeft - 8
                        )
                                & 0xFF
                );

                bitsLeft -=
                        8;
            }
        }

        return result.toByteArray();
    }

    private int getBase32Index(
            char character
    ) {
        if (
                character >= 'A'
                        && character <= 'Z'
        ) {
            return character - 'A';
        }

        if (
                character >= '2'
                        && character <= '7'
        ) {
            return character - '2' + 26;
        }

        return -1;
    }
}