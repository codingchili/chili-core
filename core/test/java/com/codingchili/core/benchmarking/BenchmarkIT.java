package com.codingchili.core.benchmarking;

import io.vertx.core.Promise;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.codingchili.core.benchmarking.reporting.BenchmarkHTMLReport;
import com.codingchili.core.context.*;

import static com.codingchili.core.configuration.CoreStrings.PARAM_ITERATIONS;

/**
 * Tests for the benchmarking subsystem
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkIT {
    private static final String STRING_ITERATIONS = "2";
    private static final int ITERATIONS = 2;
    private static CoreContext context;

    @Rule
    public Timeout timeout = new Timeout(16, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();

        SystemContext.clustered(clustering -> {
            if (clustering.succeeded()) {
                context = clustering.result();
                async.complete();
            } else {
                test.fail(clustering.cause());
            }
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

        new CoreBenchmarkSuite().setIterations(ITERATIONS).maps(context, listener)
                .onComplete(done -> {
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
        Promise<CommandResult> promise = Promise.promise();

        promise.future().onComplete(done -> {
            test.assertTrue(done.succeeded());
            async.complete();
        });

        executor.addProperty(PARAM_ITERATIONS, STRING_ITERATIONS);
        new CoreBenchmarkSuite().execute(promise, executor);
    }

    @Test
    public void testBenchmarkBuilders(TestContext test) {
        Async async = test.async();

        List<BenchmarkGroup> groups = new ArrayList<>();

        BiConsumer<BenchmarkGroup, String> addOneOperation = (group, implementation) -> {
            group.implementation(implementation)
                    .add("sleep1x", Promise::complete)
                    .add("sleep2x", Promise::complete);
        };

        BiConsumer<String, Integer> addOneGroup = (name, iterations) -> {
            BenchmarkGroup group = new BenchmarkGroupBuilder(name, iterations);
            addOneOperation.accept(group, "fastImplementation");
            addOneOperation.accept(group, "slowImplementation");
            groups.add(group);
        };

        addOneGroup.accept("group_1", ITERATIONS);
        addOneGroup.accept("group_2", ITERATIONS);
        addOneGroup.accept("group_3", ITERATIONS);

        new BenchmarkExecutor(context)
                //.setListener(new BenchmarkConsoleListener())
                .start(groups).onComplete(done -> {
            new BenchmarkHTMLReport(done.result())
                    .saveTo("wowza.html");

            async.complete();
        });
    }

}
