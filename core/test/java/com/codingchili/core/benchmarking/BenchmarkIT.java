package com.codingchili.core.benchmarking;

import com.codingchili.core.context.*;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.configuration.CoreStrings.PARAM_ITERATIONS;

/**
 * @author Robin Duda
 * <p>
 * Tests for the benchmarking subsystem
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkIT {
    private static final String STRING_ITERATIONS = "2";
    private static final int ITERATIONS = 2;
    private static CoreContext context;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();

        SystemContext.clustered(clustering -> {
            context = clustering.result();
            async.complete();
        });
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    /* simple test case that runs all map benchmarks using mock implementations
     * that handles the verification of the executor.
     *
     * - that the benchmarks does not complete before all tests have been executed.
     * - benchmarks are executed one at a time in order
     * - all events are triggered and in order for the given listener
     */
    @Test
    public void testRunBenchmarkSuites(TestContext test) {
        Async async = test.async();
        MockListener listener = new MockListener(test);

        new BenchmarkSuite().setIterations(ITERATIONS).maps(context, listener)
                .setHandler(done -> {
                    test.assertTrue(done.succeeded());
                    test.assertTrue(done.result().size() > 0);
                    listener.assertAllEventsTriggered();
                    async.complete();
                });
    }

    /*
     * Executes the benchmark suite as if it were executed from the commandline.
     */
    @Test
    public void testExecuteSuiteAsCommand(TestContext test) {
        CommandExecutor executor = new DefaultCommandExecutor();
        Async async = test.async();
        Future<CommandResult> future = Future.<CommandResult>future().setHandler(done -> {
            test.assertTrue(done.succeeded());
            async.complete();
        });
        executor.addProperty(PARAM_ITERATIONS, STRING_ITERATIONS);
        new BenchmarkSuite().execute(future, executor);
    }

}
