package com.codingchili.core.storage;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.codingchili.core.testing.MapTestCases;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the storage providers in core. Reuse these tests when new
 *         storage subsystems are implemented using the StorageLoader.
 */
@RunWith(VertxUnitRunner.class)
public class HazelMapIT extends MapTestCases {
    private static Vertx vertx;

    @BeforeClass
    public static void beforeClass(TestContext test) {
        Async async = test.async();

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            vertx = cluster.result();
            async.complete();
        });
    }

    @AfterClass
    public static void afterClass(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @After
    @Override
    public void tearDown(TestContext test) {
        // prevent shutting down the vx instance.
    }

    @Before
    public void setUp(TestContext test) {
        super.setUp(test.async(), HazelMap.class, vertx);
    }
}
