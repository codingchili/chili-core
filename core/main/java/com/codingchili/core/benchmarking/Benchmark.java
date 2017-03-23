package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *         <p>
 *         Implemented by executable benchmarks.
 */
public interface Benchmark extends BenchmarkResult {
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
    String getName();

    /**
     * Return the name of the implementation that generated the result.
     *
     * @return name of the implementation.
     */
    String getImplementation();

    /**
     * @return Return the number of iterations the benchmark is executed.
     */
    int getIterations();
}

