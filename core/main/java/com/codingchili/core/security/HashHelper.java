package com.codingchili.core.security;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.security.exception.HashMismatchException;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 * <p>
 * Handles the hashing of passwords and the generation
 * of the salts used in the hashing; internally in Argon2.
 * <p>
 * Uses mutable data types for storing plaintext passwords
 * to allow wiping. Reduces effectiveness of core dumps slightly,
 * as awaiting gc takes much longer time.
 */

public class HashHelper {
    private static final int ITERATIONS = 1;
    private static final int MEMORY = 8192;
    private static final int PARALLELISM = 4;
    private static final int HASH_LENGTH = 32;
    private static final int SALT_LENGTH = 16;
    private final static Argon2 argon2 = Argon2Factory.create(SALT_LENGTH, HASH_LENGTH);
    private CoreContext context;

    public HashHelper(CoreContext context) {
        this.context = context;
    }

    /**
     * Blocking password hashing with internal salt using ARGON2.
     *
     * @param plaintext plaintext password to be hashed.
     * @return the hash in argon format.
     */
    public String hash(String plaintext) {
        return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, plaintext);
    }

    /**
     * Verifies a plaintext password against a hashed password.
     *
     * @param future    callback
     * @param expected  the expected outcome of the hash operation.
     * @param plaintext the plaintext password to be hashed and compared to expected.
     */
    public void verify(Handler<AsyncResult<Void>> future, String expected, String plaintext) {
        context.<Boolean>blocking(blocked -> {
            blocked.complete(argon2.verify(expected, plaintext));
        }, hashed -> {
            if (hashed.result()) {
                future.handle(Future.succeededFuture());
            } else {
                future.handle(Future.failedFuture(new HashMismatchException()));
            }
        });
    }

    /**
     * Async hashing a password with an internal salt using ARGON2.
     *
     * @param future    callback for string result with hex encoded hash.
     * @param plaintext plaintext password to be hashed.
     */
    public void hash(Handler<AsyncResult<String>> future, String plaintext) {
        context.<String>blocking(blocking -> {
            blocking.complete(hash(plaintext));
        }, result -> {
            if (result.succeeded()) {
                future.handle(Future.succeededFuture(result.result()));
            } else {
                future.handle(Future.failedFuture(new HashMismatchException()));
            }
        });
    }

    /**
     * Wipes an array that contains sensitive data.
     *
     * @param sensitive the data to be wiped.
     */
    public void wipe(char[] sensitive) {
        argon2.wipeArray(sensitive);
    }
}