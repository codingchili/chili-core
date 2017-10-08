package com.codingchili.core.benchmarking;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Robin Duda
 * <p>
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
        groups.add(new MockGroup(context, "mock-group-1", ITERATIONS));
        groups.add(new MockGroup(context, "mock-group-2", ITERATIONS));
        executor = new BenchmarkExecutor(context);
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void testBenchmarksExecutedInOrder() {
        execute(done -> {

        });
    }

    private void execute(Handler<AsyncResult<List<BenchmarkGroup>>> result) {
        Future<List<BenchmarkGroup>> future = Future.future();
        future.setHandler(result);
        executor.start(future, groups);
    }

    @Test
    public void testAllBenchmarksExecuted(TestContext test) {
        execute(done -> groups.stream().map(group -> (MockGroup) group)
                .forEach(group -> test.assertTrue(group.isExecuted())));
    }

    @Test
    public void testAllImplementationsExecuted(TestContext test) {
        execute(done -> groups().forEach(group -> {
            test.assertTrue(group.getFirstImplementation().isBothBenchmarksExecuted());
            test.assertTrue(group.getSecondImplementation().isBothBenchmarksExecuted());
        }));
    }

    @Test
    public void testAllGroupsExecuted(TestContext test) {
        execute(done -> groups().forEach(group -> test.assertTrue(group.isExecuted())));
    }

    private Stream<MockGroup> groups() {
        return groups.stream().map(group -> (MockGroup) group);
    }

    @Test
    public void testVerifyBenchmarksFinished(TestContext test) {
        execute(done -> {
            groups().forEach(group -> group.getImplementations().forEach(implementation -> {
                implementation.getBenchmarks().forEach(benchmark -> {
                    test.assertTrue(benchmark.isFinished());
                });
            }));
        });
    }

    @Test
    public void testVerifyNumberOfIterations(TestContext test) {
        execute(done -> groups().forEach(group -> group.getImplementations().stream()
                .map(implementation -> (MockImplementation) implementation)
                .forEach(implementation -> {
                    test.assertEquals(ITERATIONS, implementation.getFirstBenchmarkExecutions());
                    test.assertEquals(ITERATIONS, implementation.getSecondBenchmarkExecutions());
                })));
    }
}
