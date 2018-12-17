package com.codingchili.core.benchmarking;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 * <p>
 * "Abstract" implementation of a map group.
 */
public class BenchmarkGroupBuilder implements BenchmarkGroup {
    private List<BenchmarkImplementation> implementations = new ArrayList<>();
    private int iterations;
    private int progress;
    private String name;

    public BenchmarkGroupBuilder(String name, int iterations) {
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
        implementations.forEach(implementation -> implementation.setGroup(this));
        this.implementations = implementations;
        return this;
    }

    @Override
    public BenchmarkImplementation implementation(String implementationName) {
        BenchmarkImplementation implementation = new BenchmarkImplementationBuilder(implementationName)
                .setGroup(this);

        implementations.add(implementation);
        return implementation;
    }

    @Override
    public int getProgressInterval() {
        return progress;
    }
}
