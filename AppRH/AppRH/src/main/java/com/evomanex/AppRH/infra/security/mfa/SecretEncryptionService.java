package com.evomanex.AppRH.infra.security.mfa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class SecretEncryptionService {

    private static final String TRANSFORMATION =
            "AES/GCM/NoPadding";

    private static final String KEY_ALGORITHM =
            "AES";

    private static final int REQUIRED_KEY_LENGTH =
            32;

    private static final int IV_LENGTH =
            12;

    private static final int TAG_LENGTH =
            128;

    private final SecretKey secretKey;

    private final SecureRandom secureRandom;

    public SecretEncryptionService(
            @Value("${app.mfa.encryption-key}")
            String encodedKey
    ) {
        if (
                encodedKey == null
                        || encodedKey.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "A chave de criptografia MFA é obrigatória."
            );
        }

        byte[] keyBytes;

        try {
            keyBytes =
                    Base64
                            .getDecoder()
                            .decode(
                                    encodedKey
                            );
        } catch (
                IllegalArgumentException exception
        ) {
            throw new IllegalArgumentException(
                    "A chave de criptografia MFA não está em Base64 válido.",
                    exception
            );
        }

        if (
                keyBytes.length
                        != REQUIRED_KEY_LENGTH
        ) {
            throw new IllegalArgumentException(
                    "A chave de criptografia MFA deve possuir 32 bytes."
            );
        }

        this.secretKey =
                new SecretKeySpec(
                        keyBytes,
                        KEY_ALGORITHM
                );

        this.secureRandom =
                new SecureRandom();
    }

    public String encrypt(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "O valor que será criptografado é obrigatório."
            );
        }

        try {
            byte[] iv =
                    new byte[IV_LENGTH];

            secureRandom.nextBytes(
                    iv
            );

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] encryptedValue =
                    cipher.doFinal(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            byte[] output =
                    new byte[
                            iv.length
                                    + encryptedValue.length
                            ];

            System.arraycopy(
                    iv,
                    0,
                    output,
                    0,
                    iv.length
            );

            System.arraycopy(
                    encryptedValue,
                    0,
                    output,
                    iv.length,
                    encryptedValue.length
            );

            return Base64
                    .getEncoder()
                    .encodeToString(
                            output
                    );
        } catch (
                GeneralSecurityException exception
        ) {
            throw new IllegalStateException(
                    "Não foi possível criptografar o segredo MFA.",
                    exception
            );
        }
    }

    public String decrypt(
            String encryptedValue
    ) {
        if (
                encryptedValue == null
                        || encryptedValue.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "O segredo MFA criptografado é obrigatório."
            );
        }

        byte[] input;

        try {
            input =
                    Base64
                            .getDecoder()
                            .decode(
                                    encryptedValue
                            );
        } catch (
                IllegalArgumentException exception
        ) {
            throw new IllegalStateException(
                    "Não foi possível descriptografar o segredo MFA.",
                    exception
            );
        }

        if (
                input.length
                        <= IV_LENGTH
        ) {
            throw new IllegalStateException(
                    "Não foi possível descriptografar o segredo MFA."
            );
        }

        byte[] iv =
                Arrays.copyOfRange(
                        input,
                        0,
                        IV_LENGTH
                );

        byte[] encryptedContent =
                Arrays.copyOfRange(
                        input,
                        IV_LENGTH,
                        input.length
                );

        try {
            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] decryptedValue =
                    cipher.doFinal(
                            encryptedContent
                    );

            return new String(
                    decryptedValue,
                    StandardCharsets.UTF_8
            );
        } catch (
                GeneralSecurityException exception
        ) {
            throw new IllegalStateException(
                    "Não foi possível descriptografar o segredo MFA.",
                    exception
            );
        }
    }
}