package com.codingchili.core.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.vertx.core.*;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.exception.HashMismatchException;

/**
 * Handles the hashing of passwords and the generation
 * of the salts used in the hashing; internally in Argon2.
 * <p>
 * Uses mutable data types for storing plaintext passwords
 * to allow wiping. Reduces effectiveness of core dumps slightly,
 * as awaiting gc takes much longer time.
 */

public class HashFactory {
    private static final ArgonSettings settings = Configurations.security().getArgon();
    private static final Argon2 argon2;
    private final CoreContext context;

    static {
        argon2 = Argon2Factory.create(
                settings.getSaltLength(),
                settings.getHashLength()
        );
    }

    /**
     * Creates a new hash factory - requires a context to be created.
     *
     * @param context core context to execute on.
     */
    public HashFactory(CoreContext context) {
        this.context = context;
    }

    /**
     * Verifies a plaintext password against a hashed password.
     *
     * @param future    callback
     * @param expected  the expected outcome of the hash operation.
     * @param plaintext the plaintext password to be hashed and compared to expected.
     */
    public void verify(Handler<AsyncResult<Void>> future, String expected, char[] plaintext) {
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
     * @param plaintext plaintext password to be hashed.
     * @return callback
     */
    public Future<String> hash(char[] plaintext) {
        Promise<String> promise = Promise.promise();
        context.<String>blocking(blocking -> blocking.complete(
                argon2.hash(
                        settings.getIterations(),
                        settings.getMemory(),
                        settings.getParallelism(),
                        plaintext
                )
        ), result -> {
            if (result.succeeded()) {
                promise.complete(result.result());
            } else {
                promise.fail(new HashMismatchException());
            }
        });
        return promise.future();
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