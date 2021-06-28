package com.codingchili.core.security;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * Tests for the constant time byte comparator.
 */
@RunWith(VertxUnitRunner.class)
public class ByteComparatorTest {
    private static final char[] password = "pass".toCharArray();
    private static final char[] wrong = "wrong".toCharArray();
    private HashFactory hasher;
    private CoreContext context;

    @Before
    public void setUp() {
        context = new SystemContext();
        hasher = new HashFactory(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void verifyHashCompareSuccess(TestContext test) {
        Async async = test.async();
        hasher.hash(password).onComplete(result -> {
            String hash = result.result();
            Assert.assertTrue(ByteComparator.compare(hash, hash));
            Assert.assertTrue(ByteComparator.compare(hash.getBytes(), hash.getBytes()));
            async.complete();
        });
    }

    @Test
    public void verifyHashCompareFailure(TestContext test) {
        Async async = test.async();
        hasher.hash(password).onComplete(hash1 -> {
            hasher.hash(wrong).onComplete(hash2 -> {
                Assert.assertFalse(
                        ByteComparator.compare(hash1.result(), hash2.result()));
                Assert.assertFalse(
                        ByteComparator.compare(hash1.result().getBytes(), hash2.result().getBytes()));
                async.complete();
            });
        });
    }

}
