package com.codingchili.core.security;

import io.vertx.core.WorkerExecutor;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * Tests for the HashHelper
 */
@RunWith(VertxUnitRunner.class)
public class HashFactoryTest {
    private static final int HASH_TIME_LIMIT = 10000;
    private static final int HASH_TIME_MIN = 100;
    private static final char[] PLAINTEXT_WRONG = "wrong".toCharArray();
    private static final char[] PLAINTEXT = "pass".toCharArray();
    private CoreContext context;
    private WorkerExecutor executor;
    private HashFactory hasher;

    @Before
    public void setUp() {
        context = new SystemContext();
        hasher = new HashFactory(context);
        executor = context.vertx().createSharedWorkerExecutor("worker_pool_name", 8);
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void testHashesAreUnique(TestContext test) {
        Async async = test.async();

        hasher.hash(PLAINTEXT).onComplete(hash1 -> {
            hasher.hash(PLAINTEXT).onComplete(hash2 -> {
                test.assertNotNull(hash1.result());
                test.assertTrue(hash1.result().length() != 0);
                test.assertNotEquals(hash1.result(), PLAINTEXT);
                test.assertNotEquals(hash1.result(), hash2.result());
                async.complete();
            });
        });
    }

    @Test
    public void testFailVerifyWrongPassword(TestContext test) {
        Async async = test.async();
        hasher.hash(PLAINTEXT).onComplete(hash -> {
            hasher.hash(PLAINTEXT_WRONG).onComplete(wrong -> {

                test.assertNotEquals(hash.result(), wrong.result());
                test.assertNotNull(hash.result());
                test.assertNotNull(wrong.result());
                test.assertTrue(hash.result().length() != 0);
                test.assertTrue(wrong.result().length() != 0);

                hasher.verify(result -> {
                    Assert.assertTrue(result.failed());
                    async.complete();
                }, hash.result(), "pass".toCharArray());

            });
        });
    }

    @Test
    public void testVerifySuccess(TestContext test) {
        Async async = test.async();
        hasher.hash(PLAINTEXT).onComplete(hash -> {
            hasher.verify(result -> {
                test.assertTrue(result.succeeded());
                async.complete();
            }, hash.result(), PLAINTEXT);
        });
    }

    @Test
    public void testCheckHashingNotTooFast(TestContext test) {
        Async async = test.async();
        long start = getTimeMS();
        AtomicInteger countdown = new AtomicInteger(100);

        for (int i = 0; i < 100; i++) {
            executor.<String>executeBlocking((blocking) -> {
                hasher.hash(PLAINTEXT).onComplete(blocking);
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

        hasher.hash(PLAINTEXT).onComplete(hash -> {
            test.assertTrue(hash.succeeded());
            test.assertNotNull(hash.result());
            async.complete();
        });
    }

    private long getTimeMS() {
        return Instant.now().toEpochMilli();
    }
}
