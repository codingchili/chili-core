package com.codingchili.core.benchmarking;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Base implementation of a benchmark.
 */
public class BenchmarkBuilder implements Benchmark {
    private static final String MAX_MEASURED = "+";
    private Map<String, Object> properties = new HashMap<>();
    private BenchmarkOperation operation;
    private String name;
    private Instant start;
    private int iterations;
    private int elapsedMS = -1;

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

    /**
     * @return time formatted as HH:mm:ss.SSS using the elapsed ms as source.
     */
    @Override
    public String getTimeFormatted() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date(elapsedMS - EPOCH_BASE));
    }

    /**
     * @return the number of operations per second as a formatted string.
     */
    @Override
    public String getRateFormatted() {
        if (elapsedMS == 0) {
            return String.format("%,d", 1000 * iterations) + MAX_MEASURED;
        } else {
            return String.format("%,d", getRate());
        }
    }

    /**
     * @return the number of operations per second.
     */
    @Override
    public int getRate() {
        if (elapsedMS == 0) {
            return 1000 * iterations;
        }
        return (int) (iterations / (elapsedMS/ 1000f));
    }
}
