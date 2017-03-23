package com.codingchili.core.benchmarking;

import java.time.Instant;

/**
 * @author Robin Duda
 *
 * Base implementation of a benchmark.
 */
public class AbstractBenchmark implements Benchmark {
    private BenchmarkImplementation implementation;
    private BenchmarkOperation operation;
    private BenchmarkGroup group;
    private String name;
    private Instant start;
    private int elapsedMS;

    public AbstractBenchmark(BenchmarkGroup group, BenchmarkImplementation implementation,
                             BenchmarkOperation operation, String name) {
        this.group = group;
        this.implementation = implementation;
        this.operation = operation;
        this.name = name;
    }

    @Override
    public Benchmark start() {
        this.start = Instant.now();
        return this;
    }

    @Override
    public void finish() {
        this.elapsedMS = (int) (Instant.now().toEpochMilli() - start.toEpochMilli());
        if (this.elapsedMS < 0) {
            this.elapsedMS = 0;
        }
    }

    @Override
    public long getElapsedMS() {
        return elapsedMS;
    }

    @Override
    public String getRate() {
        if (elapsedMS == 0) {
            return "> " + 1000 * group.getIterations();
        }
        return (int) (group.getIterations() / (elapsedMS / 1000f)) + "";
    }

    @Override
    public BenchmarkOperation operation() {
        return operation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getImplementation() {
        return implementation.getName();
    }

    @Override
    public int getIterations() {
        return group.getIterations();
    }
}
