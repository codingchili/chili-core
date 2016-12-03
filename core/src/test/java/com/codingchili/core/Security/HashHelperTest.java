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

/**
 * @author Robin Duda
 *
 * Tests for the HashHelper
 */
@RunWith(VertxUnitRunner.class)
public class HashHelperTest {
    private static final int HASH_TIME_LIMIT = 10000;
    private static final int HASH_TIME_MIN = 100;
    private static final String password = "pass";
    private static final String wrong = "wrong";
    private static final String salt = "salt";
    private static final String salt2 = "salt2";
    private static Vertx vertx;
    private static WorkerExecutor executor;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        executor = vertx.createSharedWorkerExecutor("worker_pool_name", 8);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void generateUniqueSaltTest() {
        HashMap<String, Boolean> salts = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            String salt = HashHelper.salt();

            Assert.assertFalse(salts.containsKey(salt));
            salts.put(salt, true);
        }
    }

    @Test
    public void checkHashesAreUnique() {
        String hash = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(wrong, salt);
        String hash3 = HashHelper.hash(password, salt2);

        Assert.assertNotNull(hash);
        Assert.assertTrue(hash.length() != 0);
        Assert.assertNotEquals(hash, password);
        Assert.assertNotEquals(hash, salt);

        Assert.assertNotEquals(hash, hash2);
        Assert.assertNotEquals(hash, hash3);
        Assert.assertNotEquals(hash2, hash3);
    }

    @Test
    public void verifyPasswordWithSalt() {
        String hash = HashHelper.hash(password, salt);

        Assert.assertEquals(hash, HashHelper.hash(password, salt));
    }

    @Test
    public void failVerifyPasswordWrongSalt() {
        String hash = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(password, salt2);

        Assert.assertNotEquals(hash, hash2);
    }

    @Test
    public void failVerifyWrongPassword() {
        String hash1 = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(wrong, salt);

        Assert.assertNotEquals(hash1, hash2);
    }

    @Test
    public void checkHashingNotTooFast(TestContext context) {
        Async async = context.async();
        long start = getTimeMS();
        AtomicInteger countdown = new AtomicInteger(100);

        for (int i = 0; i < 100; i++) {
            executor.executeBlocking((blocking) -> {
                HashHelper.hash(password, salt);
                blocking.complete();
            }, false, (result) -> {
                if (countdown.decrementAndGet() == 0) {
                    long time = getTimeMS() - start;
                    Assert.assertTrue(time < HASH_TIME_LIMIT);
                    Assert.assertTrue(time > HASH_TIME_MIN);
                    async.complete();
                }
            });
        }
    }

    @Test
    public void checkHashWithWorkerPool(TestContext test) {
        Async async = test.async();
        HashHelper hasher = new HashHelper(vertx);
        Future<String> future = Future.future();

        future.setHandler(hash -> {
            test.assertTrue(hash.succeeded());
            test.assertNotNull(hash.result());
            async.complete();
        });

        hasher.hash(future, password, salt);
    }

    private long getTimeMS() {
        return Instant.now().toEpochMilli();
    }
}
