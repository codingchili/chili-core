package com.codingchili.core.Protocols;

import com.codingchili.core.Protocols.Util.HashHelper;
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
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class HashHelperTest {
    private static final int HASH_TIME_LIMIT = 5000;
    private static final int HASH_TIME_MIN = 200;
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
            String salt = HashHelper.generateSalt();

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

    private long getTimeMS() {
        return Instant.now().toEpochMilli();
    }

    @Test
    public void verifyHashCompareSuccess() {
        String hash = HashHelper.hash(password, salt);
        Assert.assertTrue(HashHelper.compare(hash, hash));
    }

    @Test
    public void verifyHashCompareFailure() {
        String hash1 = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(wrong, salt2);

        Assert.assertFalse(HashHelper.compare(hash1, hash2));
    }
}
