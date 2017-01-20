package com.codingchili.core.security;

import io.vertx.core.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 *
 * Handles the hashing of passwords and the generation
 * of the salts used in the hashing.
 * reference: https://crackstation.net/hashing-security.htm#javasourcecode
 */

public class HashHelper {
    private static final String HASHHELPER_WORKERS = "hashhelper.workers";
    private final static int ITERATIONS = 2048;
    private final static int KEY_BITS = 512;
    private final static int SALT_BYTES = 32;
    private final static String ALGORITHM = "PBKDF2WithHmacSHA1";
    private final WorkerExecutor executor;

    public HashHelper(Vertx vertx) {
        int workers = system().getWorkerPoolSize();
        executor = vertx.createSharedWorkerExecutor(HASHHELPER_WORKERS, workers);
    }

    /**
     * Blocking password hashing with a salt using PBKDF2 with SHA1.
     *
     * @param password plaintext password to be hashed.
     * @param salt     for the hashing.
     */
    static String hash(String password, String salt) {
        try {
            PBEKeySpec key = new PBEKeySpec(password.toCharArray(), salt.getBytes(), ITERATIONS, KEY_BITS);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = keyFactory.generateSecret(key).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Async hashing a password with a salt using PBKDF2 with SHA1.
     *
     * @param password plaintext password to be hashed.
     * @param salt     for the hashing.
     */
    public void hash(Future<String> future, String password, String salt) {
        executor.<String>executeBlocking(blocking -> {
            blocking.complete(hash(password, salt));
        }, false, result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                throw new RuntimeException(result.cause());
            }
        });
    }

    /**
     * Generates a salt using Secure randoms.
     *
     * @return A generated salt of 512 bits.
     */
    public String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}