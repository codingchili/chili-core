package com.codingchili.core.security;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.testing.ContextMock;

/**
 * @author Robin Duda
 *
 * Tests for the HashHelper
 */
@RunWith(VertxUnitRunner.class)
public class HashHelperTest {
    private static final int HASH_TIME_LIMIT = 10000;
    private static final int HASH_TIME_MIN = 100;
    private static final String wrongPlaintext = "wrong";
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
        String hash = hasher.hash(password());
        String hash2 = hasher.hash(password());

        test.assertNotNull(hash);
        test.assertTrue(hash.length() != 0);
        test.assertNotEquals(hash, password());
        test.assertNotEquals(hash, hash2);
    }

    @Test
    public void testFailVerifyWrongPassword(TestContext test) {
        Async async = test.async();
        String hash = hasher.hash(password());
        String hash2 = hasher.hash(wrong());

        test.assertNotEquals(hash, hash2);
        test.assertNotNull(hash);
        test.assertNotNull(hash2);
        test.assertTrue(hash.length() != 0);
        test.assertTrue(hash2.length() != 0);

        hasher.verify(result -> {
            Assert.assertTrue(result.failed());
            async.complete();
        }, new String(password()), "other".toCharArray());
    }

    @Test
    public void testVerifySuccess(TestContext test) {
        Async async = test.async();
        String hash = hasher.hash(password());

        hasher.verify(result -> {
            test.assertTrue(result.succeeded());
            async.complete();
        }, hash, password());
    }

    @Test
    public void testCheckHashingNotTooFast(TestContext test) {
        Async async = test.async();
        long start = getTimeMS();
        AtomicInteger countdown = new AtomicInteger(100);

        for (int i = 0; i < 100; i++) {
            executor.executeBlocking((blocking) -> {
                hasher.hash(password());
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
        }, password());
    }

    @Test
    public void testWipePassword(TestContext test) {
        Account account = new Account().setPassword(new String(password()));
        hasher.wipe(account.getCharPassword());

        for (int i = 0; i < account.getCharPassword().length; i++) {
            test.assertEquals('\0', account.getCharPassword()[i]);
        }
    }

    private char[] wrong() {
        return wrongPlaintext.toCharArray();
    }

    private char[] password() {
        return PLAINTEXT.toCharArray();
    }

    private long getTimeMS() {
        return Instant.now().toEpochMilli();
    }
}
