package com.codingchili.core.benchmarking;

import java.util.*;

/**
 * Base implementation of a benchmark.
 */
public class BenchmarkBuilder implements Benchmark {
    private Map<String, Object> properties = new HashMap<>();
    private BenchmarkOperation operation;
    private String name;
    private long start;
    private int iterations;
    private long elapsedNanos = -1;

    /**
     * Creates a new benchmark builder.
     *
     * @param name the name of the benchmark to build.
     */
    public BenchmarkBuilder(String name) {
        this.name = name;
    }

    @Override
    public BenchmarkBuilder setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public BenchmarkBuilder setOperation(BenchmarkOperation operation) {
        this.operation = operation;
        return this;
    }

    @Override
    public Benchmark start() {
        this.start = System.nanoTime();
        return this;
    }

    @Override
    public void finish() {
        // nanoTime is monotonic, but keep at least 1ns so that the rate is defined.
        this.elapsedNanos = Math.max(1, System.nanoTime() - start);
    }

    @Override
    public boolean isFinished() {
        return (elapsedNanos >= 0);
    }

    @Override
    public long getElapsedMS() {
        return (elapsedNanos < 0) ? elapsedNanos : elapsedNanos / 1_000_000;
    }

    @Override
    public long getElapsedNanos() {
        return elapsedNanos;
    }

    @Override
    public BenchmarkOperation getOperation() {
        return operation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Benchmark setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String getTimeFormatted() {
        return BenchmarkResult.formatNanos(Math.max(0, elapsedNanos));
    }

    @Override
    public String getRateFormatted() {
        return String.format("%,d", getRate());
    }

    @Override
    public int getRate() {
        if (elapsedNanos <= 0) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, iterations * 1_000_000_000L / elapsedNanos);
    }
}
