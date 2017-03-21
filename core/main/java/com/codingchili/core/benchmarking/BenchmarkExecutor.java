package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 *         <p>
 *         Micro-benchmarks for storage implementations.
 *         <p>
 */
public class BenchmarkExecutor {
    private final List<BenchmarkResult> results = new ArrayList<>();
    private BenchResult measuring;
    private Boolean warmup;

    /**
     * Creates and runs a group of benchmarks. Benchmarks are run for one implementation at a time
     * and are executed in the same order as they are added to the group. The order for each
     * benchmark test is also preserved. No more than one benchmark is executed concurrently.
     *
     * @param future completed with the results of the benchmark when all benchmarks have passed.
     * @param group  a group of implementations that contains a set of benchmarks to be performed
     */
    public BenchmarkExecutor(Future<List<BenchmarkResult>> future, BenchmarkGroup group) {
        Future<Void> allImplementations = Future.succeededFuture();

        for (BenchmarkImplementation implementation : group.implementations()) {
            allImplementations = allImplementations.compose(v -> {
                Future<Void> execution = Future.future();
                implementation.initialize(
                        // on initialization: perform a warmup run that executes all benchmarks once
                        // and then call #reset on the implementation, to prepare for a recorded test run.
                        initialized -> warmup(implementation,
                        // on warmup completed: start the benchmark.
                        warmed -> benchmark(implementation,
                        // on benchark completed: call #shutdown on the benchmark implementation.
                        benched -> implementation.shutdown(execution))
                ));
                return execution;
            });
        }
        allImplementations.compose(done -> {
            future.complete(results);
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
    private void warmup(BenchmarkImplementation implementation, Handler<AsyncResult<Void>> handler) {
        Future<Void> future = Future.future();
        this.warmup = true;

        future.setHandler(done -> {
            this.warmup = false;
            implementation.reset(reset -> handler.handle(Future.succeededFuture()));
        });
        benchmark(implementation, future);
    }

    /**
     * Schedule all benchmarks for the given implementation.
     *
     * @param implementation the implementation to run benchmarks for.
     * @param future         to complete when all benchmarks has completed.
     */
    private void benchmark(BenchmarkImplementation implementation, Handler<AsyncResult<Void>> future) {
        List<Benchmark> benchmarks = implementation.benchmarks();

        Future<Void> allTests = Future.succeededFuture();
        for (Benchmark benchmark : benchmarks) {
            allTests = allTests.compose(v -> {
                Future<Void> next = Future.future();
                implementation.next(next);
                return next.compose(n -> doBench(benchmark));
            });
        }
        allTests.compose(result -> {
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
    private Future<Void> doBench(Benchmark benchmark) {
        Future<Void> future = Future.future();
        AtomicInteger scheduled = new AtomicInteger(benchmark.parallelism());
        AtomicInteger completed = new AtomicInteger(0);

        Handler<AsyncResult<Void>> scheduler = new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> event) {
                completed.incrementAndGet();

                // one of the operations scheduled in the parallelism windows completed
                // if less than iterations number of operations have been scheduled, schedule a new.
                if (scheduled.get() < benchmark.iterations()) {
                    scheduled.incrementAndGet();

                    Future<Void> run = Future.future();
                    run.setHandler(this);
                    benchmark.operation().perform(run);
                }

                // all iterations have completed.
                if (completed.get() == benchmark.iterations()) {
                    finishMeasure();
                    future.complete();
                }
            }
        };

        startMeasure(benchmark);

        // schedule operations according to parallelism settings.
        for (int i = 0; i < benchmark.parallelism(); i++) {
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
        this.measuring = new BenchResult(benchmark);
    }

    /**
     * Finish measuring of a test.
     */
    private void finishMeasure() {
        measuring.finish();

        if (!warmup) {
            results.add(measuring);
        }
    }
}
