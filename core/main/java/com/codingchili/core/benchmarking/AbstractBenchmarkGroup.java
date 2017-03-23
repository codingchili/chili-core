package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 *
 * "Abstract" implementation of a map group.
 */
public class AbstractBenchmarkGroup implements BenchmarkGroup {
    private List<BenchmarkImplementation> implementations = new ArrayList<>();
    private int iterations = 2000;
    private int parallelism = 5;
    private String name;

    public AbstractBenchmarkGroup(String name, int iterations, int parallelism) {
        this.name = name;
        this.iterations = iterations;
        this.parallelism = parallelism;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getIterations() {
        return iterations;
    }

    @Override
    public int getParallelism() {
        return parallelism;
    }

    @Override
    public List<BenchmarkImplementation> getImplementations() {
        return implementations;
    }

    @Override
    public BenchmarkGroup setImplementations(List<BenchmarkImplementation> implementations) {
        this.implementations = implementations;
        return this;
    }

    @Override
    public BenchmarkGroup add(BenchmarkImplementation benchmark) {
        implementations.add(benchmark);
        return this;
    }
}
