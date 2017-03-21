package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *
 * Implementation of a map benchmark.
 */
public class MapBenchmark implements Benchmark {
    private static final int ITERATIONS = 5000;
    private static final int PARALLELISM = 5;
    private BenchmarkOperation operation;
    private String group;
    private String implementation;
    private String name;

    public MapBenchmark(BenchmarkOperation operation, String group, String implementation, String name) {
        this.operation = operation;
        this.group = group;
        this.implementation = implementation;
        this.name = name;
    }

    @Override
    public BenchmarkOperation operation() {
        return operation;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public String implementation() {
        return implementation;
    }

    @Override
    public int iterations() {
        return ITERATIONS;
    }

    @Override
    public int parallelism() {
        return PARALLELISM;
    }
}
