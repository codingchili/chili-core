package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *         <p>
 *         Results for benchmarks.
 */
public interface BenchmarkResult {

    /**
     * Start measuring of the execution time.
     * @return fluent
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
     * @return time formatted as HH:mm:ss.SSS using the elapsed ms as source.
     */
    String getTimeFormatted();

    /**
     * @return the number of operations per second as a formatted string.
     */
    String getRateFormatted();

    /**
     * @return the number of operations per second.
     */
    int getRate();

    /**
     * @return returns true if {@link #finish()} has been called.
     */
    boolean isFinished();
}
