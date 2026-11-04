package com.evomanex.AppRH.infra.security.mfa;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

@Service
public class TotpService {

    private static final int PERIOD_SECONDS =
            30;

    private static final int DIGITS =
            6;

    private static final int SECRET_LENGTH =
            20;

    private static final String HMAC_ALGORITHM =
            "HmacSHA1";

    private static final char[] BASE32_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
                    .toCharArray();

    private final SecureRandom secureRandom;

    public TotpService() {
        this.secureRandom =
                new SecureRandom();
    }

    public String generateSecret() {
        byte[] secret =
                new byte[
                        SECRET_LENGTH
                        ];

        secureRandom.nextBytes(
                secret
        );

        return encodeBase32(
                secret
        );
    }

    public String generateOtpAuthUri(
            String issuer,
            String accountName,
            String secretBase32
    ) {
        validateText(
                issuer,
                "O nome do emissor é obrigatório."
        );

        validateText(
                accountName,
                "O nome da conta é obrigatório."
        );

        validateText(
                secretBase32,
                "O segredo TOTP é obrigatório."
        );

        String encodedIssuer =
                urlEncode(
                        issuer
                );

        String encodedAccountName =
                urlEncode(
                        accountName
                );

        return "otpauth://totp/"
                + encodedIssuer
                + ":"
                + encodedAccountName
                + "?secret="
                + secretBase32
                + "&issuer="
                + encodedIssuer
                + "&algorithm=SHA1"
                + "&digits="
                + DIGITS
                + "&period="
                + PERIOD_SECONDS;
    }

    public boolean verify(
            String secretBase32,
            String code
    ) {
        if (
                secretBase32 == null
                        || secretBase32.isBlank()
        ) {
            return false;
        }

        if (
                code == null
                        || !code.matches(
                        "\\d{6}"
                )
        ) {
            return false;
        }

        byte[] decodedSecret;

        try {
            decodedSecret =
                    decodeBase32(
                            secretBase32
                    );
        } catch (
                IllegalArgumentException exception
        ) {
            return false;
        }

        long currentCounter =
                InstantCounter.current(
                        PERIOD_SECONDS
                );

        for (
                long offset = -1;
                offset <= 1;
                offset++
        ) {
            String expectedCode =
                    generateCode(
                            decodedSecret,
                            currentCounter
                                    + offset
                    );

            if (
                    constantTimeEquals(
                            expectedCode,
                            code
                    )
            ) {
                return true;
            }
        }

        return false;
    }

    String generateCode(
            byte[] secret,
            long counter
    ) {
        if (
                secret == null
                        || secret.length == 0
        ) {
            throw new IllegalArgumentException(
                    "O segredo TOTP é obrigatório."
            );
        }

        if (counter < 0) {
            throw new IllegalArgumentException(
                    "O contador TOTP não pode ser negativo."
            );
        }

        try {
            byte[] counterBytes =
                    ByteBuffer
                            .allocate(
                                    Long.BYTES
                            )
                            .putLong(
                                    counter
                            )
                            .array();

            Mac mac =
                    Mac.getInstance(
                            HMAC_ALGORITHM
                    );

            SecretKeySpec keySpecification =
                    new SecretKeySpec(
                            secret,
                            HMAC_ALGORITHM
                    );

            mac.init(
                    keySpecification
            );

            byte[] hash =
                    mac.doFinal(
                            counterBytes
                    );

            int offset =
                    hash[
                            hash.length - 1
                            ]
                            & 0x0F;

            int binaryCode =
                    (
                            hash[offset]
                                    & 0x7F
                    ) << 24
                            | (
                            hash[offset + 1]
                                    & 0xFF
                    ) << 16
                            | (
                            hash[offset + 2]
                                    & 0xFF
                    ) << 8
                            | (
                            hash[offset + 3]
                                    & 0xFF
                    );

            int divisor =
                    powerOfTen(
                            DIGITS
                    );

            int oneTimePassword =
                    binaryCode
                            % divisor;

            return String.format(
                    Locale.ROOT,
                    "%0"
                            + DIGITS
                            + "d",
                    oneTimePassword
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Não foi possível gerar "
                            + "o código TOTP.",
                    exception
            );
        }
    }

    private String encodeBase32(
            byte[] data
    ) {
        if (
                data == null
                        || data.length == 0
        ) {
            throw new IllegalArgumentException(
                    "Os dados para codificação Base32 "
                            + "são obrigatórios."
            );
        }

        StringBuilder encoded =
                new StringBuilder();

        int buffer =
                0;

        int bitsLeft =
                0;

        for (byte currentByte : data) {
            buffer =
                    buffer << 8
                            | currentByte
                            & 0xFF;

            bitsLeft +=
                    8;

            while (bitsLeft >= 5) {
                int alphabetIndex =
                        buffer
                                >> (
                                bitsLeft - 5
                        )
                                & 0x1F;

                bitsLeft -=
                        5;

                encoded.append(
                        BASE32_ALPHABET[
                                alphabetIndex
                                ]
                );
            }
        }

        if (bitsLeft > 0) {
            int alphabetIndex =
                    buffer
                            << (
                            5 - bitsLeft
                    )
                            & 0x1F;

            encoded.append(
                    BASE32_ALPHABET[
                            alphabetIndex
                            ]
            );
        }

        return encoded.toString();
    }

    private byte[] decodeBase32(
            String encodedValue
    ) {
        validateText(
                encodedValue,
                "O segredo Base32 é obrigatório."
        );

        String normalizedValue =
                encodedValue
                        .trim()
                        .replace(
                                "=",
                                ""
                        )
                        .replace(
                                " ",
                                ""
                        )
                        .replace(
                                "-",
                                ""
                        )
                        .toUpperCase(
                                Locale.ROOT
                        );

        ByteArrayOutputStream decoded =
                new ByteArrayOutputStream();

        int buffer =
                0;

        int bitsLeft =
                0;

        for (
                int index = 0;
                index < normalizedValue.length();
                index++
        ) {
            char character =
                    normalizedValue.charAt(
                            index
                    );

            int alphabetIndex =
                    findBase32Index(
                            character
                    );

            if (alphabetIndex < 0) {
                throw new IllegalArgumentException(
                        "O segredo Base32 contém "
                                + "caracteres inválidos."
                );
            }

            buffer =
                    buffer << 5
                            | alphabetIndex;

            bitsLeft +=
                    5;

            if (bitsLeft >= 8) {
                int decodedByte =
                        buffer
                                >> (
                                bitsLeft - 8
                        )
                                & 0xFF;

                decoded.write(
                        decodedByte
                );

                bitsLeft -=
                        8;
            }
        }

        byte[] result =
                decoded.toByteArray();

        if (result.length == 0) {
            throw new IllegalArgumentException(
                    "O segredo Base32 é inválido."
            );
        }

        return result;
    }

    private int findBase32Index(
            char character
    ) {
        if (
                character >= 'A'
                        && character <= 'Z'
        ) {
            return character
                    - 'A';
        }

        if (
                character >= '2'
                        && character <= '7'
        ) {
            return character
                    - '2'
                    + 26;
        }

        return -1;
    }

    private boolean constantTimeEquals(
            String expected,
            String received
    ) {
        byte[] expectedBytes =
                expected.getBytes(
                        StandardCharsets.UTF_8
                );

        byte[] receivedBytes =
                received.getBytes(
                        StandardCharsets.UTF_8
                );

        return MessageDigest.isEqual(
                expectedBytes,
                receivedBytes
        );
    }

    private String urlEncode(
            String value
    ) {
        return URLEncoder
                .encode(
                        value,
                        StandardCharsets.UTF_8
                )
                .replace(
                        "+",
                        "%20"
                );
    }

    private int powerOfTen(
            int exponent
    ) {
        int result =
                1;

        for (
                int index = 0;
                index < exponent;
                index++
        ) {
            result *=
                    10;
        }

        return result;
    }

    private void validateText(
            String value,
            String message
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    message
            );
        }
    }

    private static final class InstantCounter {

        private InstantCounter() {
        }

        private static long current(
                int periodSeconds
        ) {
            return System
                    .currentTimeMillis()
                    / 1000L
                    / periodSeconds;
        }
    }
}