package com.codingchili.core.benchmarking;

import com.codingchili.core.context.CoreContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock implementation for a benchmark implementation.
 */
public class MockImplementationBuilder extends BenchmarkImplementationBuilder {
    private AtomicInteger firstBenchmarkExecutions = new AtomicInteger(0);
    private AtomicInteger secondBenchmarkExecutions = new AtomicInteger(0);
    private boolean firstBenchmarkExecuted = false;
    private boolean secondBenchmarkExecuted = false;

    /**
     * Creates a new mocked implementation.
     *
     * @param context context to enable asynchronous benchmarks.
     * @param group   the group the implementation is a member of
     * @param name    of the mock implementation
     */
    public MockImplementationBuilder(CoreContext context, BenchmarkGroup group, String name) {
        super(name);
        setGroup(group);
        add("benchmark#1", future -> {
            firstBenchmarkExecuted = true;
            firstBenchmarkExecutions.incrementAndGet();
            context.timer(10, event -> future.complete());
        });

        add("benchmark#2", future -> {
            secondBenchmarkExecuted = true;
            secondBenchmarkExecutions.incrementAndGet();
            context.timer(5, event -> future.complete());
        });

    }

    public Benchmark getFirstBenchmark() {
        return super.getBenchmarks().get(0);
    }

    public Benchmark getSecondBenchmark() {
        return super.getBenchmarks().get(1);
    }

    /**
     * @return returns true if both benchmarks have been executed.
     */
    public boolean isBothBenchmarksExecuted() {
        return firstBenchmarkExecuted && secondBenchmarkExecuted;
    }

    /**
     * @return the number of iterations executed for benchmark.
     */
    public int getFirstBenchmarkExecutions() {
        return firstBenchmarkExecutions.get();
    }

    /**
     * @return the number of iterations executed for benchmark.
     */
    public int getSecondBenchmarkExecutions() {
        return secondBenchmarkExecutions.get();
    }
}
