package com.codingchili.core.benchmarking;

import com.codingchili.core.context.CoreContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Duda
 * <p>
 * Micro-benchmarks runner.
 * <p>
 * Creates and runs a group of benchmarks. Benchmarks are run for one implementation at a time
 * and are executed in the same order as they are added to the group. The order for each
 * benchmark test is also preserved. No more than one benchmark is executed concurrently.
 */
public class BenchmarkExecutor {
    private BenchmarkListener listener = new BaseBenchmarkListener();
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
     * @param future completed with the results of the benchmark when all benchmarks have passed.
     * @param group  a group of implementations that contains a set of benchmarks to be performed
     */
    public void start(Future<List<BenchmarkGroup>> future, BenchmarkGroup group) {
        List<BenchmarkGroup> list = new ArrayList<>();
        list.add(group);
        start(future, list);
    }

    /**
     * @param future completed with the results of the benchmark when all benchmarks have passed.
     * @param groups a list of groups of implementations that contains a set of benchmarks to be performed
     */
    public void start(Future<List<BenchmarkGroup>> future, List<BenchmarkGroup> groups) {
        Future<BenchmarkGroup> allGroups = Future.succeededFuture();

        for (BenchmarkGroup group : groups) {
            allGroups = allGroups.compose(v -> {
                Future<BenchmarkGroup> benchmark = Future.future();
                listener.onGroupStarted(group);
                executeImplementations(benchmark, group);
                return benchmark;
            });
        }
        allGroups.compose(done -> {
            future.complete(groups);
            return Future.succeededFuture();
        });
    }

    private void executeImplementations(Future<BenchmarkGroup> future, BenchmarkGroup group) {
        Future<Void> allImplementations = Future.succeededFuture();

        for (BenchmarkImplementation implementation : group.getImplementations()) {
            allImplementations = allImplementations.compose(v -> {
                Future<Void> execution = Future.future();

                // on initialization: perform a warmup run that executes all benchmarks once
                // and then call #reset on the implementation, to prepare for a recorded test run.
                implementation.initialize(context,
                        initialized -> warmup(group, implementation,
                                warmed -> benchmark(group, implementation,
                                        benched -> implementation.shutdown(execution))));
                return execution;
            });
        }
        allImplementations.setHandler(done -> {
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
        Future<Void> future = Future.future();
        warmup.set(true);
        listener.onImplementationWarmup(implementation);

        future.setHandler(done -> {
            warmup.set(false);
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
    private void benchmark(BenchmarkGroup group, BenchmarkImplementation implementation,
                           Handler<AsyncResult<Void>> future) {
        List<Benchmark> benchmarks = implementation.getBenchmarks();

        if (!warmup.get()) {
            listener.onImplementationTestBegin(implementation);
        }

        Future<Void> allTests = Future.succeededFuture();
        for (Benchmark benchmark : benchmarks) {
            allTests = allTests.compose(v -> {
                Future<Void> next = Future.future();
                implementation.next(next);
                return next.compose(n -> doBench(group, benchmark));
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
        Future<Void> future = Future.future();
        AtomicInteger completed = new AtomicInteger(0);
        benchmark.start();

        for (int i = 0; i < group.getIterations(); i++) {
            Future<Void> iteration = Future.<Void>future().setHandler(done -> {
                if (completed.incrementAndGet() >= group.getIterations()) {
                    if (future.tryComplete()) {
                        benchmark.finish();
                        if (!warmup.get()) {
                            listener.onBenchmarkCompleted(benchmark);
                        }
                    }
                } else if (completed.get() % group.getProgressInterval() == 0) {
                    listener.onProgressUpdate(benchmark, completed.get());
                }
            });
            benchmark.operation().perform(iteration);
        }
        return future;
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
