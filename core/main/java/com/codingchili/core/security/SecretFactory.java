package com.codingchili.core.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static io.vertx.core.json.impl.JsonUtil.BASE64_ENCODER;

/**
 * Generates and verifies secret keys.
 */
public abstract class SecretFactory {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a random secret of the given length.
     *
     * @param bytes length of the secret in bytes.
     * @return number of random bytes specified.
     */
    public static String generate(int bytes) {
        byte[] secret = new byte[bytes];
        random.nextBytes(secret);
        return BASE64_ENCODER.encodeToString(secret);
    }

    /**
     * Verifies that two given secrets are equal.
     *
     * @param origin the kept secret.
     * @param clone  the secret to verify.
     * @return true if an exact match is found.
     */
    /**
     * Generates a key pair for signing tokens, see {@link TokenFactory#signer(com.codingchili.core.context.CoreContext,
     * byte[], byte[])}: the private key PKCS#8 encoded, the public key X.509 encoded.
     *
     * @return a new key pair of {@link com.codingchili.core.configuration.system.SecuritySettings#getKeyPairAlgorithm()}.
     */
    public static KeyPair keyPair() {
        try {
            return KeyPairGenerator.getInstance(
                    com.codingchili.core.files.Configurations.security().getKeyPairAlgorithm()).generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new com.codingchili.core.context.CoreRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param key a private or public key.
     * @return the encoded key in base64, as configuration files hold it.
     */
    public static String encode(java.security.Key key) {
        return BASE64_ENCODER.encodeToString(key.getEncoded());
    }

    public static boolean verify(String origin, String clone) {
        return ByteComparator.compare(origin.getBytes(), clone.getBytes());
    }
}
