package com.codingchili.core.Security;

import java.security.SecureRandom;

/**
 * @author Robin Duda
 */
class SecretFactory {
    private static final SecureRandom random = new SecureRandom();

    static byte[] generate(int bytes) {
        byte[] secret = new byte[bytes];
        random.nextBytes(secret);
        return secret;
    }
}
