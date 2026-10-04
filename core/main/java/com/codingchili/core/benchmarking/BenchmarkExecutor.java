package com.codingchili.core.benchmarking;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.context.CoreContext;

/**
 * Micro-benchmarks runner.
 * <p>
 * Creates and runs a group of benchmarks. Benchmarks are run for one implementation at a time
 * and are executed in the same order as they are added to the group. The order for each
 * benchmark test is also preserved. No more than one benchmark is executed concurrently.
 */
public class BenchmarkExecutor {
    private BenchmarkListener listener = new BenchmarkListener() {
    };
    private AtomicBoolean warmup = new AtomicBoolean(true);
    private CoreContext context;

    /**
     * Creates a new benchmarkexecutor that executes on the given context.
     *
     * @param context the context to execute on.
     */
    public BenchmarkExecutor(CoreContext context) {
        this.context = context;
    }

    /**
     * @param group a group of implementations that contains a set of benchmarks to be performed
     * @return future completed when benchmarks are done.
     */
    public Future<List<BenchmarkGroup>> start(BenchmarkGroup group) {
        return start(Collections.singletonList(group));
    }

    /**
     * @param groups a list of groups of implementations that contains a set of benchmarks to be performed
     * @return future completed when benchmarks are done, fails if any implementation fails to
     * initialize, reset or shut down.
     */
    public Future<List<BenchmarkGroup>> start(List<BenchmarkGroup> groups) {
        Future<Void> allGroups = Future.succeededFuture();

        for (BenchmarkGroup group : groups) {
            allGroups = allGroups.compose(v -> executeImplementations(group));
        }
        return allGroups.map(groups);
    }

    private Future<Void> executeImplementations(BenchmarkGroup group) {
        Future<Void> allImplementations = Future.succeededFuture();
        listener.onGroupStarted(group);

        for (BenchmarkImplementation implementation : group.getImplementations()) {
            // on initialization: perform a warmup run that executes all benchmarks once
            // and then call #reset on the implementation, to prepare for a recorded test run.
            allImplementations = allImplementations
                    .compose(v -> implementation.initialize(context))
                    .compose(initialized -> warmup(group, implementation))
                    .compose(warmed -> benchmark(group, implementation))
                    .compose(benched -> implementation.shutdown());
        }
        return allImplementations.onComplete(done -> listener.onGroupCompleted(group));
    }

    /**
     * Runs through the benchmark once without recording results as warmup.
     * Calls #reset on the benchmark implementation to prepare for a benchmark run.
     *
     * @param implementation the implementation to warmup.
     * @return future completed when the warmup is done and the implementation is reset.
     */
    private Future<Void> warmup(BenchmarkGroup group, BenchmarkImplementation implementation) {
        warmup.set(true);
        listener.onImplementationWarmup(implementation);

        return benchmark(group, implementation).compose(done -> {
            warmup.set(false);
            listener.onImplementationWarmupComplete(implementation);
            return implementation.reset();
        });
    }

    /**
     * Schedule all benchmarks for the given implementation.
     *
     * @param implementation the implementation to run benchmarks for.
     * @return future completed when all benchmarks have completed.
     */
    private Future<Void> benchmark(BenchmarkGroup group, BenchmarkImplementation implementation) {
        if (!warmup.get()) {
            listener.onImplementationTestBegin(implementation);
        }

        Future<Void> allTests = Future.succeededFuture();
        for (Benchmark benchmark : implementation.getBenchmarks()) {
            allTests = allTests
                    .compose(v -> implementation.next())
                    .compose(n -> doBench(group, benchmark));
        }
        return allTests.onSuccess(done -> {
            if (!warmup.get()) {
                listener.onImplementationCompleted(implementation);
            }
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
        Promise<Void> promise = Promise.promise();
        AtomicInteger completed = new AtomicInteger(0);
        benchmark.start();

        for (int i = 0; i < group.getIterations(); i++) {
            // the outcome of an operation is not recorded, failures count as completed iterations.
            benchmark.getOperation().perform().onComplete(done -> {
                if (completed.incrementAndGet() == group.getIterations()) {
                    benchmark.finish();
                    if (!warmup.get()) {
                        listener.onBenchmarkCompleted(benchmark);
                    }
                    promise.complete();
                } else if (completed.get() % group.getProgressInterval() == 0) {
                    listener.onProgressUpdate(benchmark, completed.get());
                }
            });
        }
        return promise.future();
    }

    /**
     * Sets the executor event listener.
     *
     * @param listener the listener to execute on events.
     * @return fluent
     */
    public BenchmarkExecutor setListener(BenchmarkListener listener) {
        this.listener = listener;
        return this;
    }
}
