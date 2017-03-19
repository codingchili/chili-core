package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 *         <p>
 *         Micro-benchmarks for storage implementations.
 *
 *         todo add future synchronization to allow execution of multiple implementations to be executed
 *         todo add manual synchronization for multiple benchmark groups
 *         todo add abstract implementations for Map*Benchmark classes to promote reuse.
 */
public class BenchmarkExecutor {
    private final List<BenchmarkResult> results = new ArrayList<>();
    private BenchmarkResult measuring;

    /**
     * Creates and runs a group of benchmarks.
     *
     * @param future completed with the results of the benchmark when all benchmarks have passed.
     * @param group  a group of implementations that contains a set of benchmarks to be performed
     */
    public BenchmarkExecutor(Future<List<BenchmarkResult>> future, BenchmarkGroup group) {
        for (BenchmarkImplementation implementation : group.implementations()) {
            implementation.initialize(initialized -> warmup(done ->
                    implementation.reset(reset ->
                            benchmark(future, implementation)),
                    implementation));
        }
    }

    private void warmup(Handler<AsyncResult<Void>> handler, BenchmarkImplementation implementation) {
        Future<List<BenchmarkResult>> future = Future.future();

        future.setHandler(done -> {
            results.clear();
            handler.handle(Future.succeededFuture());
        });
        benchmark(future, implementation);
    }

    private void benchmark(Future<List<BenchmarkResult>> future, BenchmarkImplementation implementation) {
        List<Benchmark> benchmarks = implementation.benchmarks();

        Future<Void> allTests = Future.succeededFuture();
        for (Benchmark benchmark : benchmarks) {
            allTests = allTests.compose(v ->
                    doBench(benchmark, implementation.implementationName()));
        }
        allTests.setHandler(result -> future.complete(results));
    }

    private Future<Void> doBench(Benchmark benchmark, String group) {
        Future<Void> future = Future.future();
        startMeasure(benchmark, group);
        benchmark.operation().perform(future);

        future.setHandler(done -> finishMeasure());
        return future;
    }

    /**
     * Starts measuring of a test.
     */
    private void startMeasure(Benchmark benchmark, String name) {
        this.measuring = new BenchmarkResult(benchmark, name);
    }

    /**
     * Finish measuring of a test.
     */
    private void finishMeasure() {
        measuring.finish();
        results.add(measuring);
    }
}
