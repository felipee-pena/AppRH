package com.evomanex.AppRH.infra.security.mfa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretEncryptionServiceTest {

    private SecretEncryptionService
            encryptionService;

    @BeforeEach
    void setUp() {
        byte[] keyBytes =
                "12345678901234567890123456789012"
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        String encodedKey =
                Base64
                        .getEncoder()
                        .encodeToString(
                                keyBytes
                        );

        encryptionService =
                new SecretEncryptionService(
                        encodedKey
                );
    }

    @Test
    void shouldEncryptAndDecryptValue() {
        String originalValue =
                "TOTP-SECRET-APP-RH";

        String encryptedValue =
                encryptionService.encrypt(
                        originalValue
                );

        String decryptedValue =
                encryptionService.decrypt(
                        encryptedValue
                );

        assertThat(
                encryptedValue
        ).isNotBlank();

        assertThat(
                encryptedValue
        ).isNotEqualTo(
                originalValue
        );

        assertThat(
                decryptedValue
        ).isEqualTo(
                originalValue
        );
    }

    @Test
    void shouldGenerateDifferentEncryptedValuesForSameInput() {
        String originalValue =
                "TOTP-SECRET-APP-RH";

        String firstEncryptedValue =
                encryptionService.encrypt(
                        originalValue
                );

        String secondEncryptedValue =
                encryptionService.encrypt(
                        originalValue
                );

        assertThat(
                firstEncryptedValue
        ).isNotEqualTo(
                secondEncryptedValue
        );

        assertThat(
                encryptionService.decrypt(
                        firstEncryptedValue
                )
        ).isEqualTo(
                originalValue
        );

        assertThat(
                encryptionService.decrypt(
                        secondEncryptedValue
                )
        ).isEqualTo(
                originalValue
        );
    }

    @Test
    void shouldRejectNullValueDuringEncryption() {
        assertThatThrownBy(
                () ->
                        encryptionService.encrypt(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O valor que será criptografado é obrigatório."
                );
    }

    @Test
    void shouldRejectBlankValueDuringEncryption() {
        assertThatThrownBy(
                () ->
                        encryptionService.encrypt(
                                " "
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O valor que será criptografado é obrigatório."
                );
    }

    @Test
    void shouldRejectNullValueDuringDecryption() {
        assertThatThrownBy(
                () ->
                        encryptionService.decrypt(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O segredo MFA criptografado é obrigatório."
                );
    }

    @Test
    void shouldRejectBlankValueDuringDecryption() {
        assertThatThrownBy(
                () ->
                        encryptionService.decrypt(
                                " "
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "O segredo MFA criptografado é obrigatório."
                );
    }

    @Test
    void shouldRejectInvalidEncryptedValue() {
        assertThatThrownBy(
                () ->
                        encryptionService.decrypt(
                                "invalid-encrypted-value"
                        )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "Não foi possível descriptografar o segredo MFA."
                );
    }

    @Test
    void shouldRejectKeyThatDoesNotContainThirtyTwoBytes() {
        byte[] invalidKeyBytes =
                "short-key"
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        String invalidEncodedKey =
                Base64
                        .getEncoder()
                        .encodeToString(
                                invalidKeyBytes
                        );

        assertThatThrownBy(
                () ->
                        new SecretEncryptionService(
                                invalidEncodedKey
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "A chave de criptografia MFA deve possuir 32 bytes."
                );
    }

    @Test
    void shouldRejectKeyThatIsNotValidBase64() {
        assertThatThrownBy(
                () ->
                        new SecretEncryptionService(
                                "not-valid-base64"
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "A chave de criptografia MFA não está em Base64 válido."
                );
    }

    @Test
    void shouldRejectNullEncryptionKey() {
        assertThatThrownBy(
                () ->
                        new SecretEncryptionService(
                                null
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "A chave de criptografia MFA é obrigatória."
                );
    }

    @Test
    void shouldRejectBlankEncryptionKey() {
        assertThatThrownBy(
                () ->
                        new SecretEncryptionService(
                                " "
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "A chave de criptografia MFA é obrigatória."
                );
    }
}