package com.codingchili.core.storage;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;


/**
 * @author Robin Duda
 * <p>
 * Tests for the CQEngine indexed storage.
 */
@RunWith(VertxUnitRunner.class)
public class IndexedMapVolatileTest extends MapTestCases {

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, IndexedMapVolatile.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }
}
