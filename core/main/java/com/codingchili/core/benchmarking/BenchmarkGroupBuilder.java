package com.codingchili.core.benchmarking;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * "Abstract" implementation of a map group.
 */
public class BenchmarkGroupBuilder implements BenchmarkGroup {
    private Map<String, BenchmarkImplementation> implementations = new HashMap<>();
    private int iterations;
    private int progress;
    private String name;

    /**
     * @param name the name of the group.
     * @param iterations number of iterations to perform for each benchmark.
     */
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
    public Collection<BenchmarkImplementation> getImplementations() {
        return new ArrayList<>(implementations.values());
    }

    @Override
    public BenchmarkGroup setImplementations(Map<String, BenchmarkImplementation> implementations) {
        implementations.forEach((key, value) -> value.setGroup(this));
        this.implementations = implementations;
        return this;
    }

    @Override
    public BenchmarkImplementation implementation(String implementationName) {
        implementations.putIfAbsent(implementationName, new BenchmarkImplementationBuilder(implementationName)
                .setGroup(this));

        return implementations.get(implementationName);
    }

    @Override
    public int getProgressInterval() {
        return progress;
    }

    @Override
    public void add(BenchmarkImplementation implementation) {
        implementations.put(implementation.getName(), implementation);
    }
}
