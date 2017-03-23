package com.codingchili.core.benchmarking;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 *         <p>
 *         Micro-benchmarks runner.
 *         <p>
 *         Creates and runs a group of benchmarks. Benchmarks are run for one implementation at a time
 *         and are executed in the same order as they are added to the group. The order for each
 *         benchmark test is also preserved. No more than one benchmark is executed concurrently.
 *         <p>
 */
public class BenchmarkExecutor {
    private BenchmarkListener listener = new AbstractBenchmarkListener();
    private Benchmark measuring;

    /**
     * @param future completed with the results of the benchmark when all benchmarks have passed.
     * @param groups a list of groups of implementations that contains a set of benchmarks to be performed
     */
    public BenchmarkExecutor(Future<List<BenchmarkGroup>> future, List<BenchmarkGroup> groups) {
        Future<BenchmarkGroup> allGroups = Future.succeededFuture();

        for (BenchmarkGroup group : groups) {
            allGroups.compose(v -> {
                Future<BenchmarkGroup> benchmark = Future.future();
                executeImplementations(benchmark, group);
                return benchmark;
            });
        }
        allGroups.compose(done -> {
            future.complete(groups);
            return Future.succeededFuture();
        });
    }

    /**
     * @param future completed with the results of the benchmark when all benchmarks have passed.
     * @param group  a group of implementations that contains a set of benchmarks to be performed
     */
    public BenchmarkExecutor(Future<BenchmarkGroup> future, BenchmarkGroup group) {
        executeImplementations(future, group);
    }

    private void executeImplementations(Future<BenchmarkGroup> future, BenchmarkGroup group) {
        Future<Void> allImplementations = Future.succeededFuture();

        for (BenchmarkImplementation implementation : group.getImplementations()) {
            allImplementations = allImplementations.compose(v -> {
                Future<Void> execution = Future.future();

                // on initialization: perform a warmup run that executes all benchmarks once
                // and then call #reset on the implementation, to prepare for a recorded test run.
                implementation.initialize(
                        initialized -> warmup(group, implementation,
                                warmed -> benchmark(group, implementation,
                                        benched -> implementation.shutdown(execution))));
                return execution;
            });
        }
        allImplementations.compose(done -> {
            listener.onGroupCompleted(group);
            future.complete(group);
            return future;
        });
    }

    /**
     * Runs through the benchmark once without recording results as warmup.
     * Calls #reset on the benchmark implementation to prepare for a benchmark run.
     *
     * @param implementation the implementation to warmup.
     * @param handler        the handler to call when completed.
     */
    private void warmup(BenchmarkGroup group, BenchmarkImplementation implementation, Handler<AsyncResult<Void>>
            handler) {
        Future<Void> future = Future.future();

        future.setHandler(done -> {
            listener.onImplementationWarmupComplete(implementation);
            implementation.reset(reset -> handler.handle(Future.succeededFuture()));
        });
        benchmark(group, implementation, future);
    }

    /**
     * Schedule all benchmarks for the given implementation.
     *
     * @param implementation the implementation to run benchmarks for.
     * @param future         to complete when all benchmarks has completed.
     */
    private void benchmark(BenchmarkGroup group, BenchmarkImplementation implementation, Handler<AsyncResult<Void>>
            future) {
        List<Benchmark> benchmarks = implementation.getBenchmarks();

        Future<Void> allTests = Future.succeededFuture();
        for (Benchmark benchmark : benchmarks) {
            allTests = allTests.compose(v -> {
                Future<Void> next = Future.future();
                implementation.next(next);
                return next.compose(n -> doBench(group, benchmark));
            });
        }
        allTests.compose(result -> {
            listener.onImplementationCompleted(implementation);
            future.handle(Future.succeededFuture());
            return Future.succeededFuture();
        });
    }

    /**
     * Performs the actual benchmarking by measuring the time taken to execute the given
     * benchmarks operation.
     *
     * @param benchmark the benchmark to execute
     * @return a future that is completed when the benchmark is completed.
     */
    private Future<Void> doBench(BenchmarkGroup group, Benchmark benchmark) {
        Future<Void> future = Future.future();
        AtomicInteger completed = new AtomicInteger(0);

        Handler<AsyncResult<Void>> scheduler = new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                int done = completed.incrementAndGet();

                // one of the operations scheduled in the parallelism windows completed
                // if less than iterations number of operations have been scheduled, schedule a new.
                if (done < group.getIterations()) {
                    Future<Void> run = Future.future();
                    run.setHandler(this);
                    benchmark.operation().perform(run);
                }

                if (done % group.getProgressInterval() == 0) {
                    listener.onProgressUpdate(benchmark, done);
                }

                // all iterations have completed.
                if (done == group.getIterations()) {
                    finishMeasure();
                    listener.onBenchmarkCompleted(benchmark);
                    future.complete();
                }
            }
        };

        startMeasure(benchmark);

        // schedule operations according to parallelism settings.
        for (int i = 0; i < group.getParallelism(); i++) {
            Future<Void> run = Future.future();
            run.setHandler(scheduler);
            benchmark.operation().perform(run);
        }
        return future;
    }

    /**
     * Starts measuring of a test.
     */
    private void startMeasure(Benchmark benchmark) {
        this.measuring = benchmark.start();
    }

    /**
     * Finish measuring of a test.
     */
    private void finishMeasure() {
        measuring.finish();
    }

    /**
     * Sets the executor event listener.
     *
     * @param listener the listener to execute on events.
     */
    public void setListener(BenchmarkListener listener) {
        this.listener = listener;
    }
}
