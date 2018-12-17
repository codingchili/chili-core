package com.codingchili.core.benchmarking;

import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * Implemented by executable benchmarks.
 */
public interface Benchmark extends BenchmarkResult {
    /**
     * The operation to benchmark.
     *
     * @return an executable benchmark.
     */
    BenchmarkOperation getOperation();

    /**
     * The handler of the single benchmark operation to run. Should typically
     * match the method handler.
     *
     * @return a string that identifies the handler the Benchmark operation.
     */
    String getName();

    /**
     * Sets a property on the benchmark object, may be a parameter or a result.
     *
     * @param key   the key to identify this property
     * @param value the value to insert
     * @return fluent
     */
    Benchmark setProperty(String key, Object value);

    /**
     * get all properties added to the implementation.
     *
     * @return a map of all the properties that has been set.
     */
    Map<String, Object> getProperties();

    /**
     * Start measuring of the execution time.
     *
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
     * @return returns true if {@link #finish()} has been called.
     */
    boolean isFinished();

    /**
     * @param iterations the number of iterations this benchmark will be executed for.
     */
    Benchmark setIterations(int iterations);

    /**
     * @param name the name of the benchmark to set.
     */
    void setName(String name);
}

