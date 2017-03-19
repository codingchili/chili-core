package com.codingchili.core.benchmarking;

import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 *         <p>
 *         Implemented by executable benchmarks.
 */
public interface Benchmark
{
    /**
     * The operation to benchmark.
     *
     * @return an executable benchmark.
     */
    BenchmarkOperation operation();

    /**
     * The name of the single benchmark operation to run. Should typically
     * match the method name.
     *
     * @return a string that identifies the name the Benchmark operation.
     */
    String testName();

    /**
     * Number of iterations that the benchmark operation is to be executed.
     *
     * @return a integer indicating the number of operation iterations.
     */
    int iterations();

    /**
     * @return the number of operations per second.
     */
    int ratePerSecond();

    /**
     * @return the average time per operation iteration.
     */
    TimeUnit average();

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
