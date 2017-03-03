package com.codingchili.core.storage;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.testing.MapTestCases;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the CQEngine indexed storage.
 */
@Ignore("Travis: disabling tests one by one.")
@RunWith(VertxUnitRunner.class)
public class IndexedMapTest extends MapTestCases {

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, IndexedMap.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }

    @Ignore("Searching with case insensitivity is not supported for CQEngine.")
    @Override
    public void testCaseSensitivityLikeIgnored(TestContext test) {
    }
}
