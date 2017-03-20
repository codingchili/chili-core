package com.codingchili.core.benchmarking;

import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 *
 * Results for benchmarks.
 */
public interface BenchmarkResult extends Benchmark {

    /**
     * Finish benchmarking: calculates the results.
     */
    void finish();

    /**
     * @return the time taken to complete the benchmark in ms.
     */
    long elapsedMS();

    /**
     * @return the number of operations per second.
     */
    int rate();

    /**
     * @return the name of the test group.
     */
    String group();

    /**
     * @return the name of the implementation that executed the benchmark.
     */
    String implementation();

    /**
     * @return the number of operations per second.
     */
    int ratePerSecond();

    /**
     * @return the average time per operation iteration.
     */
    TimeUnit timePerIteration();

    /**
     * @return the max time to execute an operation.
     */
    TimeUnit max();

    /**
     * @return the total time required to execute all iterations of
     * the given operation.
     */
    TimeUnit total();
}
