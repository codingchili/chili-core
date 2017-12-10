package com.codingchili.core.storage;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Tests for the storage providers in core. Reuse these tests when new
 * storage subsystems are implemented using the StorageLoader.
 */
@RunWith(VertxUnitRunner.class)
public class HazelMapIT extends MapTestCases {
    private static CoreContext context;

    @BeforeClass
    public static void beforeClass(TestContext test) {
        Async async = test.async();

        SystemContext.clustered(clustering -> {
            test.assertTrue(clustering.succeeded());
            context = clustering.result();
            async.complete();
        });
    }

    @AfterClass
    public static void afterClass(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @After
    @Override
    public void tearDown(TestContext test) {
        // prevent shutting down the vx instance.
    }

    @Before
    public void setUp(TestContext test) {
        super.setUp(test.async(), HazelMap.class, context);
    }
}
