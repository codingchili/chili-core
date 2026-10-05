package com.codingchili.core.benchmarking;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.codingchili.core.benchmarking.ProtocolBenchmarkImplementation.DispatchHandler;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * Tests the benchmark that compares the ways a protocol can invoke a route.
 */
@RunWith(VertxUnitRunner.class)
public class ProtocolBenchmarkTest {
    private static final int ITERATIONS = 2;
    private CoreContext context;

    @Rule
    public Timeout timeout = new Timeout(30, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        context = new SystemContext();
    }

    @After
    public void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testAllStrategiesAreBenchmarkedWithAnEqualNumberOfCalls(TestContext test) throws Throwable {
        new BenchmarkExecutor(context).start(ProtocolBenchmarkImplementation.group(ITERATIONS))
                .onComplete(test.asyncAssertSuccess(groups -> {
                    test.assertEquals(1, groups.size());

                    List<String> names = groups.get(0).getImplementations().stream()
                            .map(BenchmarkImplementation::getName)
                            .collect(Collectors.toList());

                    test.assertEquals(List.of("Method.invoke", "MethodHandle", "LambdaMetafactory",
                            "Direct call (baseline)", "Protocol.process (end to end)"), names);

                    groups.get(0).getImplementations().forEach(implementation -> {
                        test.assertEquals(3, implementation.getBenchmarks().size());
                        implementation.getBenchmarks().forEach(benchmark -> {
                            test.assertTrue(benchmark.isFinished());
                            test.assertTrue(benchmark.getRate() > 0);
                        });
                    });
                }));
    }

    @Test
    public void testRateIsCallsPerSecond(TestContext test) throws Throwable {
        new BenchmarkExecutor(context).start(ProtocolBenchmarkImplementation.group(ITERATIONS))
                .onComplete(test.asyncAssertSuccess(groups -> {
                    Benchmark benchmark = groups.get(0).getImplementations().iterator().next()
                            .getBenchmarks().get(0);

                    long calls = (long) ITERATIONS * ProtocolBenchmarkImplementation.CALLS_PER_ITERATION;
                    long expected = calls * 1_000_000_000L / benchmark.getElapsedNanos();

                    // the rate is an int, rounded down.
                    test.assertTrue(Math.abs(expected - benchmark.getRate()) <= 1);
                }));
    }

    @Test
    public void testFailsWhenRoutesCannotBeBound(TestContext test) {
        BenchmarkGroup group = new BenchmarkGroupBuilder("unbound", ITERATIONS);

        group.add(new ProtocolBenchmarkImplementation(group, "unbound", new DispatchHandler(),
                request -> {
                }, () -> {
        }, () -> {
            throw new NoSuchMethodException("route");
        }));

        new BenchmarkExecutor(context).start(group).onComplete(test.asyncAssertFailure(e -> {
            test.assertTrue(e instanceof NoSuchMethodException);
        }));
    }

    @Test
    public void testFailsWhenTheHandlerDoesNotReceiveTheCalls(TestContext test) {
        BenchmarkGroup group = new BenchmarkGroupBuilder("broken", ITERATIONS);

        // a route that is bound to something else than the handler that counts the calls.
        group.add(new ProtocolBenchmarkImplementation(group, "broken", new DispatchHandler(),
                request -> {
                }, () -> {
        }, () -> new Object()));

        new BenchmarkExecutor(context).start(group).onComplete(test.asyncAssertFailure(e -> {
            test.assertTrue(e instanceof IllegalStateException);
            test.assertTrue(e.getMessage().contains("broken"));
        }));
    }
}
