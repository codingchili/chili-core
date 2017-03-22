package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *
 * Results for benchmarks.
 */
public interface BenchmarkResult {

    /**
     * Start measuring of the execution time.
     */
    Benchmark start();

    /**
     * Finish benchmarking: calculates the results.
     */
    void finish();

    /**
     * @return the time taken to complete the benchmark in ms.
     */
    long getElapsedMS();

    /**
     * @return the number of operations per second.
     */
    int getRate();
}
