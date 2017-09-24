package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 * <p>
 * "Abstract" implementation of a map group.
 */
public class BaseBenchmarkGroup implements BenchmarkGroup {
    private List<BenchmarkImplementation> implementations = new ArrayList<>();
    private int iterations = 1000;
    private int progress;
    private String name;

    public BaseBenchmarkGroup(String name, int iterations) {
        this.name = name;
        this.iterations = iterations;
        this.progress = (iterations / 20);

        if (progress == 0) {
            progress = 1;
        }
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

    @Override
    public int getProgressInterval() {
        return progress;
    }
}
