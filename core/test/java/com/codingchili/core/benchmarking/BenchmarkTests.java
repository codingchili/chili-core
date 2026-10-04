package com.codingchili.core.benchmarking;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * Tests base implementations of benchmark groups, implementations and benchmarks.
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkTests {
    private static final int ITERATIONS = 3;
    private List<BenchmarkGroup> groups = new ArrayList<>();
    private BenchmarkExecutor executor;
    private CoreContext context;

    @Before
    public void setUp() {
        context = new SystemContext();
        groups.add(new MockGroupBuilder(context, "mock-group-1", ITERATIONS));
        groups.add(new MockGroupBuilder(context, "mock-group-2", ITERATIONS));
        executor = new BenchmarkExecutor(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testBenchmarksExecutedInOrder(TestContext test) {
        execute(test, done -> test.assertEquals(groups, done));
    }

    private void execute(TestContext test, Consumer<List<BenchmarkGroup>> assertions) {
        executor.start(groups).onComplete(test.asyncAssertSuccess(assertions::accept));
    }

    @Test
    public void testAllBenchmarksExecuted(TestContext test) {
        execute(test, done -> groups.stream().map(group -> (MockGroupBuilder) group)
                .forEach(group -> test.assertTrue(group.isExecuted())));
    }

    @Test
    public void testAllImplementationsExecuted(TestContext test) {
        execute(test, done -> groups().forEach(group -> {
            test.assertTrue(group.getFirstImplementation().isBothBenchmarksExecuted());
            test.assertTrue(group.getSecondImplementation().isBothBenchmarksExecuted());
        }));
    }

    @Test
    public void testAllGroupsExecuted(TestContext test) {
        execute(test, done -> groups().forEach(group -> test.assertTrue(group.isExecuted())));
    }

    private Stream<MockGroupBuilder> groups() {
        return groups.stream().map(group -> (MockGroupBuilder) group);
    }

    @Test
    public void testVerifyBenchmarksFinished(TestContext test) {
        execute(test, done -> {
            groups().forEach(group -> group.getImplementations().forEach(implementation -> {
                implementation.getBenchmarks().forEach(benchmark -> {
                    test.assertTrue(benchmark.isFinished());
                });
            }));
        });
    }

    @Test
    public void testVerifyNumberOfIterations(TestContext test) {
        execute(test, done -> groups().forEach(group -> group.getImplementations().stream()
                .map(implementation -> (MockImplementationBuilder) implementation)
                .forEach(implementation -> {
                    // each benchmark runs once for warmup and once recorded.
                    test.assertEquals(ITERATIONS * 2, implementation.getFirstBenchmarkExecutions());
                    test.assertEquals(ITERATIONS * 2, implementation.getSecondBenchmarkExecutions());
                })));
    }
}
