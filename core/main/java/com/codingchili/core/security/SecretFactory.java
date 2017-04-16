package com.codingchili.core.security;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author Robin Duda
 *         <p>
 *         Generates and verifies secret keys.
 */
abstract class SecretFactory {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a random secret of the given length.
     *
     * @param bytes length of the secret in bytes.
     * @return number of random bytes specified.
     */
    static String generate(int bytes) {
        byte[] secret = new byte[bytes];
        random.nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }

    /**
     * Verifies that two given secrets are equal.
     *
     * @param origin the kept secret.
     * @param clone  the secret to verify.
     * @return true if an exact match is found.
     */
    static boolean verify(String origin, String clone) {
        return ByteComparator.compare(origin.getBytes(), clone.getBytes());
    }
}
