package com.codingchili.core.benchmarking;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Robin Duda
 *         <p>
 *         Mock implementation for a benchmark implementation.
 */
public class MockImplementation extends BaseBenchmarkImplementation {
    private AtomicInteger firstBenchmarkExecutions = new AtomicInteger(0);
    private AtomicInteger secondBenchmarkExecutions = new AtomicInteger(0);
    private boolean firstBenchmarkExecuted = false;
    private boolean secondBenchmarkExecuted = false;

    /**
     * Creates a new mocked implementation.
     *
     * @param group the group the implementation is a member of
     * @param name  of the mock implementation
     */
    public MockImplementation(BenchmarkGroup group, String name) {
        super(group, name);
        add(future -> {
            firstBenchmarkExecuted = true;
            firstBenchmarkExecutions.incrementAndGet();
            future.complete();
        }, "benchmark#1");

        add(future -> {
            secondBenchmarkExecuted = true;
            secondBenchmarkExecutions.incrementAndGet();
            future.complete();
        }, "benchmark#2");
    }

    private Benchmark getFirstBenchmark() {
        return super.getBenchmarks().get(0);
    }

    private Benchmark getSecondBenchmark() {
        return super.getBenchmarks().get(1);
    }

    /**
     * @return returns true if both benchmarks have been executed.
     */
    public boolean isBothBenchmarksExecuted() {
        return firstBenchmarkExecuted && secondBenchmarkExecuted;
    }
}
