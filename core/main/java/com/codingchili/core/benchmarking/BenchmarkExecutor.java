package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.streams.impl.PumpImpl;

/**
 * @author Robin Duda
 *         <p>
 *         Micro-benchmarks for storage implementations.
 *         <p>
 */
public class BenchmarkExecutor {
    private final List<BenchResult> results = new ArrayList<>();
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
    public BenchmarkExecutor(Future<List<BenchResult>> future, BenchmarkGroup group) {
        Future<Void> allImplementations = Future.succeededFuture();

        for (BenchmarkImplementation implementation : group.implementations()) {
            allImplementations.compose(v -> {
                // when the benchmark suite is completed for an implementation, call its shutdown method.
                Future<Void> run = Future.<Void>future()
                        .setHandler(executed -> implementation.shutdown(done -> {
                        }));

                // initialize the implementation, call warmup to perform all benchmarks once without
                // recording the results. Call reset to prepare for another run, and then execute
                // the benchmark suite again and record the results.
                implementation.initialize(initialized -> warmup(implementation, done ->
                        implementation.reset(reset -> benchmark(run, implementation))
                ));
                return run;
            });
        }
        allImplementations.compose(done -> {
            future.complete(results);
            return future;
        });
    }

    /**
     * Runs through the benchmark once without recording results as warmup.
     *
     * @param implementation the implementation to warmup.
     * @param handler        the handler to call when completed.
     */
    private void warmup(BenchmarkImplementation implementation, Handler<AsyncResult<Void>> handler) {
        Future<Void> future = Future.future();
        this.warmup = true;

        future.setHandler(done -> {
            this.warmup = false;
            handler.handle(Future.succeededFuture());
        });
        benchmark(future, implementation);
    }

    /**
     * Schedule all benchmarks for the given implementation.
     *
     * @param future         to complete when all benchmarks has completed.
     * @param implementation the implementation to run benchmarks for.
     */
    private void benchmark(Future<Void> future, BenchmarkImplementation implementation) {
        List<Benchmark> benchmarks = implementation.benchmarks();

        Future<Void> allTests = Future.succeededFuture();
        for (Benchmark benchmark : benchmarks) {
            allTests = allTests.compose(v ->
                    doBench(benchmark));
        }
        allTests.compose(result -> {
            implementation.reset(done -> {
                future.complete();
            });
            return future;
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
                // if less than iterations number of operations are scheduled, schedule a new.
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
