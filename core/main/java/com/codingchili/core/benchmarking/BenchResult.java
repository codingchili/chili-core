package com.codingchili.core.benchmarking;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 *         <p>
 *         Contains the results of a single benchmark run.
 */
public class BenchResult extends MapBenchmark implements BenchmarkResult {
    private Instant start;
    private int elapsedMS;

    public BenchResult(Benchmark benchmark) {
        super(benchmark.operation(), benchmark.group(), benchmark.implementation(), benchmark.name());
        this.start = Instant.now();
    }

    @Override
    public void finish() {
        this.elapsedMS = (Instant.now().getNano() - start.getNano()) / (1000 * 1000);
    }

    @Override
    public long elapsedMS() {
        return elapsedMS;
    }

    @Override
    public int rate() {
        int rate = iterations() / (elapsedMS + 1);
        return (rate == 0) ? iterations() : rate;
    }

    @Override
    public int ratePerSecond() {
        return 0;
    }

    @Override
    public TimeUnit timePerIteration() {
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

    @Override
    public String toString() {
        return "[" + group() + "]" + implementation() + " executed " +
                name() + " and finished in " + elapsedMS + "ms. [" + rate() + " op/s]";
    }
}