package com.codingchili.core.storage;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.testing.MapTestCases;

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
            new HazelMapIT().setUp(async, HazelMap.class, vertx);
        });
    }

    @AfterClass
    public static void afterClass(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}
}
