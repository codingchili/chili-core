package com.codingchili.core.benchmarking;

import java.time.Instant;

/**
 * @author Robin Duda
 *
 * Implementation of a map benchmark.
 */
public class MapBenchmark implements Benchmark, BenchmarkResult {
    private BenchmarkGroup group;
    private BenchmarkImplementation implementation;
    private BenchmarkOperation operation;
    private Instant start;
    private int elapsedMS;

    private String name;

    public MapBenchmark(BenchmarkGroup group, BenchmarkImplementation implementation,
                        BenchmarkOperation operation, String name) {
        this.group = group;
        this.implementation = implementation;
        this.operation = operation;
        this.name = name;
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
    public Benchmark start() {
        this.start = Instant.now();
        return this;
    }

    @Override
    public void finish() {
        this.elapsedMS = (Instant.now().getNano() - start.getNano()) / (1000 * 1000);
        if (this.elapsedMS < 0) {
            this.elapsedMS = 0;
        }
    }

    @Override
    public long getElapsedMS() {
        return elapsedMS;
    }

    @Override
    public int getRate() {
        float time = (1.0f / ((elapsedMS == 0) ? 1 : elapsedMS));
        if (time == 0.0f) {
            time = 1;
        }
        return (int) (group.getIterations() / time);
    }
}
