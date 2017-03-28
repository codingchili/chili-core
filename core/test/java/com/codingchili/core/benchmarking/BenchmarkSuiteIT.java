package com.codingchili.core.benchmarking;

import static com.codingchili.core.configuration.CoreStrings.PARAM_ITERATIONS;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CommandExecutor;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *
 * Integration tests for the benchmark suite.
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkSuiteIT {

    @Test
    @Ignore("stuck when creating report.")
    public void testExecuteSuite(TestContext test) {
        CommandExecutor executor = new CommandExecutor();
        Async async = test.async();
        Future<Void> future = Future.<Void>future().setHandler(done -> async.complete());
        executor.addProperty(PARAM_ITERATIONS, "1");
        new BenchmarkSuite().execute(future, executor);
    }
}
