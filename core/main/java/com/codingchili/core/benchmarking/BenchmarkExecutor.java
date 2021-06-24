package com.codingchili.core.benchmarking;

import io.vertx.core.*;

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
        Promise<List<BenchmarkGroup>> promise = Promise.promise();
        start(Collections.singletonList(group)).onComplete(promise);
        return promise.future();
    }

    /**
     * @param groups a list of groups of implementations that contains a set of benchmarks to be performed
     * @return future completed when benchmarks are done.
     */
    public Future<List<BenchmarkGroup>> start(List<BenchmarkGroup> groups) {
        Promise<List<BenchmarkGroup>> promise = Promise.promise();
        Future<BenchmarkGroup> allGroups = Future.succeededFuture();

        for (BenchmarkGroup group : groups) {
            allGroups = allGroups.compose(v -> {
                Promise<BenchmarkGroup> benchmark = Promise.promise();
                listener.onGroupStarted(group);
                executeImplementations(benchmark, group);
                return benchmark.future();
            });
        }
        allGroups.compose(done -> {
            promise.complete(groups);
            return Future.succeededFuture();
        });
        return promise.future();
    }

    private void executeImplementations(Promise<BenchmarkGroup> future, BenchmarkGroup group) {
        Future<Void> allImplementations = Future.succeededFuture();

        for (BenchmarkImplementation implementation : group.getImplementations()) {
            allImplementations = allImplementations.compose(v -> {
                Promise<Void> execution = Promise.promise();

                // on initialization: perform a warmup run that executes all benchmarks once
                // and then call #reset on the implementation, to prepare for a recorded test run.
                implementation.initialize(context,
                        initialized -> warmup(group, implementation,
                                warmed -> benchmark(group, implementation,
                                        benched -> implementation.shutdown(execution))));
                return execution.future();
            });
        }
        allImplementations.onComplete(done -> {
            listener.onGroupCompleted(group);
            future.complete(group);
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
        Promise<Void> promise = Promise.promise();
        warmup.set(true);
        listener.onImplementationWarmup(implementation);

        promise.future().onComplete(done -> {
            warmup.set(false);
            listener.onImplementationWarmupComplete(implementation);
            implementation.reset(reset -> handler.handle(Future.succeededFuture()));
        });
        benchmark(group, implementation, promise);
    }

    /**
     * Schedule all benchmarks for the given implementation.
     *
     * @param implementation the implementation to run benchmarks for.
     * @param future         to complete when all benchmarks has completed.
     */
    private void benchmark(BenchmarkGroup group, BenchmarkImplementation implementation,
                           Handler<AsyncResult<Void>> future) {
        List<Benchmark> benchmarks = implementation.getBenchmarks();

        if (!warmup.get()) {
            listener.onImplementationTestBegin(implementation);
        }

        Future<Void> allTests = Future.succeededFuture();
        for (Benchmark benchmark : benchmarks) {
            allTests = allTests.compose(v -> {
                Promise<Void> next = Promise.promise();
                implementation.next(next);
                return next.future().compose(n -> doBench(group, benchmark));
            });
        }
        allTests.compose(result -> {
            if (!warmup.get()) {
                listener.onImplementationCompleted(implementation);
            }
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
        Promise<Void> promise = Promise.promise();
        AtomicInteger completed = new AtomicInteger(0);
        benchmark.start();

        for (int i = 0; i < group.getIterations(); i++) {
            Promise<Void> iteration = Promise.promise();
            iteration.future().onComplete(done -> {
                if (completed.incrementAndGet() == group.getIterations()) {
                    if (!warmup.get()) {
                        listener.onBenchmarkCompleted(benchmark);
                    }
                    if (promise.tryComplete()) {
                        benchmark.finish();
                    }
                } else if (completed.get() % group.getProgressInterval() == 0) {
                    listener.onProgressUpdate(benchmark, completed.get());
                }
            });
            benchmark.getOperation().perform(iteration);
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
