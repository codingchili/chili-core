package com.codingchili.core.security;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 *
 * Tests for the constant time byte comparator.
 */
@RunWith(VertxUnitRunner.class)
public class ByteComparatorTest {
    private static final String password = "pass";
    private static final String wrong = "wrong";
    private static final String salt = "salt";
    private static final String salt2 = "salt2";

    @Test
    public void verifyHashCompareSuccess() {
        String hash = HashHelper.hash(password, salt);
        Assert.assertTrue(ByteComparator.compare(hash, hash));
        Assert.assertTrue(ByteComparator.compare(hash.getBytes(), hash.getBytes()));
    }

    @Test
    public void verifyHashCompareFailure() {
        String hash1 = HashHelper.hash(password, salt);
        String hash2 = HashHelper.hash(wrong, salt2);

        Assert.assertFalse(ByteComparator.compare(hash1, hash2));
        Assert.assertFalse(ByteComparator.compare(hash1.getBytes(), hash2.getBytes()));
    }

}
