package com.codingchili.core.security;

import com.codingchili.core.context.SystemContext;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Duda
 * <p>
 * Tests for the HashHelper
 */
@RunWith(VertxUnitRunner.class)
public class HashHelperTest {
    private static final int HASH_TIME_LIMIT = 10000;
    private static final int HASH_TIME_MIN = 100;
    private static final String PLAINTEXT_WRONG = "wrong";
    private static final String PLAINTEXT = "pass";
    private Vertx vertx;
    private WorkerExecutor executor;
    private HashHelper hasher;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        executor = vertx.createSharedWorkerExecutor("worker_pool_name", 8);
        hasher = new HashHelper(new SystemContext(vertx));
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testHashesAreUnique(TestContext test) {
        String hash = hasher.hash(PLAINTEXT);
        String hash2 = hasher.hash(PLAINTEXT);

        test.assertNotNull(hash);
        test.assertTrue(hash.length() != 0);
        test.assertNotEquals(hash, PLAINTEXT);
        test.assertNotEquals(hash, hash2);
    }

    @Test
    public void testFailVerifyWrongPassword(TestContext test) {
        Async async = test.async();
        String hash = hasher.hash(PLAINTEXT);
        String hash2 = hasher.hash(PLAINTEXT_WRONG);

        test.assertNotEquals(hash, hash2);
        test.assertNotNull(hash);
        test.assertNotNull(hash2);
        test.assertTrue(hash.length() != 0);
        test.assertTrue(hash2.length() != 0);

        hasher.verify(result -> {
            Assert.assertTrue(result.failed());
            async.complete();
        }, PLAINTEXT, "other");
    }

    @Test
    public void testVerifySuccess(TestContext test) {
        Async async = test.async();
        String hash = hasher.hash(PLAINTEXT);

        hasher.verify(result -> {
            test.assertTrue(result.succeeded());
            async.complete();
        }, hash, PLAINTEXT);
    }

    @Test
    public void testCheckHashingNotTooFast(TestContext test) {
        Async async = test.async();
        long start = getTimeMS();
        AtomicInteger countdown = new AtomicInteger(100);

        for (int i = 0; i < 100; i++) {
            executor.executeBlocking((blocking) -> {
                hasher.hash(PLAINTEXT);
                blocking.complete();
            }, false, (result) -> {
                if (countdown.decrementAndGet() == 0) {
                    long time = getTimeMS() - start;
                    test.assertTrue(time < HASH_TIME_LIMIT);
                    test.assertTrue(time > HASH_TIME_MIN);
                    async.complete();
                }
            });
        }
    }

    @Test
    public void testCheckHashingWithWorkerPool(TestContext test) {
        Async async = test.async();

        hasher.hash(hash -> {
            test.assertTrue(hash.succeeded());
            test.assertNotNull(hash.result());
            async.complete();
        }, PLAINTEXT);
    }

    private long getTimeMS() {
        return Instant.now().toEpochMilli();
    }
}
