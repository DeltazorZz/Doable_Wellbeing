package com.dw.backend.doablewellbeingbackend.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class PasswordHashingService {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 310_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private final SecureRandom random = new SecureRandom();

    @Value("${app.security.password.pepper:}")
    private String pepper;

    /**
     * Generates a new random salt for a user (16 bytes)
     */
    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash a password using PBKDF2WithHmacSHA256 + salt + optional pepper
     */
    public String hash(char[] password, byte[] salt) {
        char[] withPepper = null;
        try {
            // combine password + pepper
            if (pepper != null && !pepper.isBlank()) {
                withPepper = (new String(password) + pepper).toCharArray();
            } else {
                withPepper = Arrays.copyOf(password, password.length);
            }

            PBEKeySpec spec = new PBEKeySpec(withPepper, salt, ITERATIONS, KEY_LENGTH_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] derived = skf.generateSecret(spec).getEncoded();

            // encode hash to Base64 for varchar column storage
            return Base64.getEncoder().encodeToString(derived);

        } catch (Exception e) {
            throw new IllegalStateException("Error while hashing password", e);
        } finally {
            // clear password data from memory
            if (withPepper != null) Arrays.fill(withPepper, '\0');
            Arrays.fill(password, '\0');
        }
    }


    public boolean verify(char[] password, byte[] salt, String expectedBase64) {
        String candidate = hash(password, salt);
        return constantTimeEquals(candidate, expectedBase64);
    }


    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes();
        byte[] y = b.getBytes();
        if (x.length != y.length) return false;
        int r = 0;
        for (int i = 0; i < x.length; i++) {
            r |= x[i] ^ y[i];
        }
        return r == 0;
    }
}
