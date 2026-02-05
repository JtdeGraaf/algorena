package com.algorena.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("NullAway")
class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private static final String TEST_ENCRYPTION_KEY = "zXf1bOvMgwonGWc/5lEKj+zRaInI13ky1Tdlo18IINU="; // 32 bytes

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService(TEST_ENCRYPTION_KEY);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void encrypt_ShouldReturnNullForNullOrEmptyInput(String input) {
        String encrypted = encryptionService.encrypt(input);
        assertThat(encrypted).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void decrypt_ShouldReturnNullForNullOrEmptyInput(String input) {
        String decrypted = encryptionService.decrypt(input);
        assertThat(decrypted).isNull();
    }

    @Test
    void encrypt_ShouldReturnBase64EncodedString() {
        String plaintext = "secret-api-key-123";
        String encrypted = encryptionService.encrypt(plaintext);

        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEmpty();
        // Should be valid Base64
        assertThatNoException().isThrownBy(() -> Base64.getDecoder().decode(encrypted));
    }

    @Test
    void encrypt_ShouldProduceDifferentCiphertextsForSameInput() {
        String plaintext = "secret-api-key";
        String encrypted1 = encryptionService.encrypt(plaintext);
        String encrypted2 = encryptionService.encrypt(plaintext);

        // Due to random IV, same plaintext should produce different ciphertexts
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void encrypt_ShouldProduceLongerOutputThanInput() {
        String plaintext = "short";
        String encrypted = encryptionService.encrypt(plaintext);

        assertThat(encrypted).isNotNull();
        // Encrypted output includes IV (12 bytes) + ciphertext + GCM tag (16 bytes)
        // Base64 encoding increases size by ~33%
        assertThat(encrypted.length()).isGreaterThan(plaintext.length());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-valid-base64!@#", "invalid@data", "!!!###"})
    void decrypt_ShouldThrowForInvalidBase64(String invalidData) {
        assertThatThrownBy(() -> encryptionService.decrypt(invalidData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void decrypt_ShouldThrowForCorruptedData() {
        String validEncrypted = encryptionService.encrypt("test");
        // Corrupt the encrypted data
        String corrupted = validEncrypted.substring(0, validEncrypted.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> encryptionService.decrypt(corrupted))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @ParameterizedTest
    @MethodSource("providePlaintextTestCases")
    void encryptAndDecrypt_ShouldReturnOriginalPlaintext(String plaintext) {
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    static Stream<String> providePlaintextTestCases() {
        return Stream.of(
                "my-secret-api-key-12345",
                "key!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`", // Special characters
                "密钥-🔑-key-αβγ-ключ", // Unicode characters
                "x".repeat(1000), // Long string
                "a", // Very short string
                "  key with spaces  \n\t", // Whitespace
                "line1\nline2\rline3\r\nline4", // Line breaks
                "tab\tseparated\tvalues" // Tabs
        );
    }

    @Test
    void decrypt_ShouldFailWithDifferentKey() {
        String plaintext = "secret-key";
        String encrypted = encryptionService.encrypt(plaintext);

        // Create a different encryption service with a different key
        String differentKey = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="; // Different 32-byte key
        EncryptionService differentService = new EncryptionService(differentKey);

        // Should fail to decrypt with wrong key
        assertThatThrownBy(() -> differentService.decrypt(encrypted))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    void encryptAndDecrypt_ShouldAlwaysReturnOriginal() {
        String plaintext = "api-key-test";

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plaintext);
    }
}
