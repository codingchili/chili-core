package com.codingchili.core.benchmarking;

import java.time.Instant;

/**
 * @author Robin Duda
 *         <p>
 *         Contains the results of a single benchmark run.
 */
public class BenchmarkResult {
    private Benchmark benchmark;
    private Instant start;
    private int elapsedMS;
    private String group;

    public BenchmarkResult(Benchmark benchmark, String group) {
        this.benchmark = benchmark;
        this.group = group;
        this.start = Instant.now();
    }

    public void finish() {
        this.elapsedMS = (Instant.now().getNano() - start.getNano()) / (1000 * 1000);
    }

    public int iterations() {
        return benchmark.iterations();
    }

    public long elapsedMS() {
        return elapsedMS;
    }

    public String name() {
        return group;
    }

    public int rate() {
        int rate = iterations() / (elapsedMS + 1);
        return (rate == 0) ? iterations() : rate;
    }

    @Override
    public String toString() {
        return benchmark.testName() + " executed " + this.group + " and finished in " +
                elapsedMS + "ms. [" + rate() + " op/s]";
    }
}