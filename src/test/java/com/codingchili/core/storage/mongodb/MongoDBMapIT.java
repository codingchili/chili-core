package com.codingchili.core.storage.mongodb;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.storage.MongoDBMap;
import com.codingchili.core.testing.MapTestCases;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the storage providers in core. Reuse these tests when new
 *         storage subsystems are implemented using the StorageLoader.
 */
@Ignore
@RunWith(VertxUnitRunner.class)
public class MongoDBMapIT extends MapTestCases {

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, MongoDBMap.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }
}
