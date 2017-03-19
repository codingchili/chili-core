package com.codingchili.core.benchmarking;

import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 *
 * Implementation of a map benchmark.
 */
public class MapBenchmark implements Benchmark {
    private static final int ITERATIONS = 99500;
    private BenchmarkOperation operation;
    private String name;

    public MapBenchmark(BenchmarkOperation operation, String name) {
        this.operation = operation;
        this.name = name;
    }

    @Override
    public BenchmarkOperation operation() {
        return operation;
    }

    @Override
    public String testName() {
        return name;
    }

    @Override
    public int iterations() {
        return ITERATIONS;
    }

    @Override
    public int ratePerSecond() {
        return 0;
    }

    @Override
    public TimeUnit average() {
        return null;
    }

    @Override
    public TimeUnit max() {
        return null;
    }

    @Override
    public TimeUnit total() {
        return null;
    }
}
