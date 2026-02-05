package com.algorena.security;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting sensitive data using AES-256-GCM.
 * <p>
 * Uses Galois/Counter Mode (GCM) which provides both confidentiality and authenticity.
 * Each encryption operation generates a unique 12-byte IV (nonce) which is prepended to the ciphertext.
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12; // bytes (96 bits recommended for GCM)

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public EncryptionService(@Value("${algorena.encryption.key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be 256 bits (32 bytes)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     *
     * @param plaintext the text to encrypt (null returns null)
     * @return Base64-encoded string containing IV + ciphertext, or null if input is null
     */
    @Nullable
    public String encrypt(@Nullable String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Return as Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts ciphertext that was encrypted with AES-256-GCM.
     *
     * @param encryptedData Base64-encoded string containing IV + ciphertext (null returns null)
     * @return decrypted plaintext, or null if input is null
     */
    @Nullable
    public String decrypt(@Nullable String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }

        try {
            // Decode Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
