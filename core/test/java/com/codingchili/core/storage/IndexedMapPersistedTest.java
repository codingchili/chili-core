package com.codingchili.core.storage;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * Tests for the CQEngine indexed storage.
 */
//@Ignore("Slow to execute.")
@RunWith(VertxUnitRunner.class)
public class IndexedMapPersistedTest extends MapTestCases {

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, IndexedMapPersisted.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }
}
