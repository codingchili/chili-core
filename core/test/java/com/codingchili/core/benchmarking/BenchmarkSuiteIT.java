package com.codingchili.core.benchmarking;

import static com.codingchili.core.configuration.CoreStrings.PARAM_ITERATIONS;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CommandExecutor;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *         <p>
 *         Integration tests for the benchmark suite.
 */
@RunWith(VertxUnitRunner.class)
public class BenchmarkSuiteIT {

    @Test
    public void testExecuteSuite(TestContext test) {
        CommandExecutor executor = new CommandExecutor();
        Async async = test.async();
        Future<Void> future = Future.<Void>future().setHandler(done -> {
            if (done.succeeded()) {
                async.complete();
            } else {
                test.fail(done.cause().getMessage());
            }
        });
        executor.addProperty(PARAM_ITERATIONS, "5");
        new BenchmarkSuite().execute(future, executor);
    }
}
