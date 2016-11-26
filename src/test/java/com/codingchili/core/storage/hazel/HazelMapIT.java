package com.codingchili.core.storage.hazel;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.*;
import com.codingchili.core.testing.MapTestCases;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the storage providers in core. Reuse these tests when new
 *         storage subsystems are implemented using the StorageLoader.
 */
@RunWith(VertxUnitRunner.class)
public class HazelMapIT extends MapTestCases {

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            super.setUp(async, HazelMap.class, cluster.result());
        });
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }
}
