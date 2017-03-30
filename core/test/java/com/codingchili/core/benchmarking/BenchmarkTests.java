package com.codingchili.core.benchmarking;

import io.vertx.core.*;
import org.junit.*;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * @author Robin Duda
 *
 * Tests base implementations of benchmark groups, implementations and benchmarks.
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkTests {
    private List<BenchmarkGroup> groups = new ArrayList<>();
    private BenchmarkExecutor executor;
    private CoreContext context;

    @Before
    public void setUp() {
        context = new SystemContext(Vertx.vertx());
        groups.add(new MockGroup(context, "mock-group-1", 1));
        groups.add(new MockGroup(context, "mock-group-2", 2));
        executor = new BenchmarkExecutor(context);
    }

    @After
    public void tearDown() {
        context.vertx().close();
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
    public void testAllBenchmarksExecuted() {
        execute(done -> {

        });
    }

    @Test
    public void testAllImplementationsExecuted() {
        execute(done -> {

        });
    }

    @Test
    public void testAllGroupsExecuted() {
        execute(done -> {

        });
    }

    @Test
    public void testVerifyBenchmarkResults() {
        execute(done -> {

        });
    }

    @Test
    public void testVerifyNumberOfIterations() {
        execute(done -> {

        });
    }
}
