package com.codingchili.core.security;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.SystemContext;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the constant time byte comparator.
 */
@RunWith(VertxUnitRunner.class)
public class ByteComparatorTest {
    private static final String password = "pass";
    private static final String wrong = "wrong";
    private HashHelper hasher;
    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        hasher = new HashHelper(new SystemContext(vertx));
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void verifyHashCompareSuccess(TestContext test) {
        Async async = test.async();
        hasher.hash(result -> {
            String hash = result.result();
            Assert.assertTrue(ByteComparator.compare(hash, hash));
            Assert.assertTrue(ByteComparator.compare(hash.getBytes(), hash.getBytes()));
            async.complete();
        }, password);
    }

    @Test
    public void verifyHashCompareFailure(TestContext test) {
        Async async = test.async();
        hasher.hash(hash1 -> {
            hasher.hash(hash2 -> {
                Assert.assertFalse(
                        ByteComparator.compare(hash1.result(), hash2.result()));
                Assert.assertFalse(
                        ByteComparator.compare(hash1.result().getBytes(), hash2.result().getBytes()));
                async.complete();
            }, wrong);
        }, password);
    }

}
