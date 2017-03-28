package com.codingchili.core.benchmarking;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.SystemContext;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the benchmarking subsystem
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkIT {
    private static final int ITERATIONS = 25;
    private static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            vertx = cluster.result();
            async.complete();
        });
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    /* simple test case that runs all map benchmarks using mock implementations
     * that handles the verification of the executor.
     *
     * - that the benchmarks does not complete before all tests have been executed.
     * - benchmarks are executed one at a time in order
     * - all events are triggered and in order for the given listener
     */
    public void testRunBenchmarkSuites(TestContext test) {
        Async async = test.async();
        SystemContext context = new SystemContext(vertx);
        MockListener listener = new MockListener(test);

        new BenchmarkSuite().setIterations(ITERATIONS).maps(context, listener)
                .setHandler(done -> {
                    test.assertTrue(done.succeeded());
                    test.assertTrue(done.result().size() > 0);
                    listener.assertAllEventsTriggered();
                    async.complete();
                });
    }

}
