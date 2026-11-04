package com.evomanex.AppRH.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashServiceTest {

    private HashService hashService;

    @BeforeEach
    void setUp() {
        hashService =
                new HashService();
    }

    @Test
    void shouldGenerateSha256Hash() {
        String hash =
                hashService.sha256(
                        "AppRH"
                );

        assertThat(
                hash
        ).isNotNull();

        assertThat(
                hash
        ).hasSize(
                64
        );
    }

    @Test
    void shouldGenerateSameHashForSameValue() {
        String firstHash =
                hashService.sha256(
                        "same-value"
                );

        String secondHash =
                hashService.sha256(
                        "same-value"
                );

        assertThat(
                firstHash
        ).isEqualTo(
                secondHash
        );
    }

    @Test
    void shouldGenerateDifferentHashesForDifferentValues() {
        String firstHash =
                hashService.sha256(
                        "first-value"
                );

        String secondHash =
                hashService.sha256(
                        "second-value"
                );

        assertThat(
                firstHash
        ).isNotEqualTo(
                secondHash
        );
    }

    @Test
    void shouldGenerateLowercaseHexadecimalHash() {
        String hash =
                hashService.sha256(
                        "AppRH"
                );

        assertThat(
                hash
        ).matches(
                "[0-9a-f]{64}"
        );
    }

    @Test
    void shouldGenerateExpectedHashForKnownValue() {
        String hash =
                hashService.sha256(
                        "abc"
                );

        assertThat(
                hash
        ).isEqualTo(
                "ba7816bf8f01cfea414140de5dae2223"
                        + "b00361a396177a9cb410ff61f20015ad"
        );
    }

    @Test
    void shouldGenerateHashForEmptyValue() {
        String hash =
                hashService.sha256(
                        ""
                );

        assertThat(
                hash
        ).isEqualTo(
                "e3b0c44298fc1c149afbf4c8996fb924"
                        + "27ae41e4649b934ca495991b7852b855"
        );
    }
}