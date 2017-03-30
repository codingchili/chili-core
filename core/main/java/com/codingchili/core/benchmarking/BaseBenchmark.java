package com.codingchili.core.benchmarking;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * @author Robin Duda
 *
 * Base implementation of a benchmark.
 */
public class BaseBenchmark implements Benchmark {
    private static final int EPOCH_BASE = 3600000;
    private static final String MAX_MEASURED = "+";
    private static final String DATE_FORMAT = "HH:mm:ss.SSS";
    private Map<String, Object> properties = new HashMap<>();
    private BenchmarkImplementation implementation;
    private BenchmarkOperation operation;
    private BenchmarkGroup group;
    private String name;
    private Instant start;
    private int elapsedMS = -1;

    public BaseBenchmark(BenchmarkGroup group, BenchmarkImplementation implementation,
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
    public boolean isFinished() {
        return (elapsedMS >= 0);
    }

    @Override
    public long getElapsedMS() {
        return elapsedMS;
    }

    @Override
    public String getTimeFormatted() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date(getElapsedMS() - EPOCH_BASE));
    }

    @Override
    public String getRateFormatted() {
        if (elapsedMS == 0) {
            return String.format("%,d", 1000 * group.getIterations()) + MAX_MEASURED;
        } else {
            return String.format("%,d", getRate());
        }
    }

    @Override
    public int getRate() {
        if (elapsedMS == 0) {
            return 1000 * group.getIterations();
        }
        return (int) (group.getIterations() / (elapsedMS / 1000f));
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

    @Override
    public Benchmark setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
}
