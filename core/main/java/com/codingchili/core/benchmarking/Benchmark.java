package com.codingchili.core.benchmarking;

import java.util.Map;

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
     * The handler of the single benchmark operation to run. Should typically
     * match the method handler.
     *
     * @return a string that identifies the handler the Benchmark operation.
     */
    String getName();

    /**
     * Return the handler of the implementation that generated the result.
     *
     * @return handler of the implementation.
     */
    String getImplementation();

    /**
     * @return Return the number of iterations the benchmark is executed.
     */
    int getIterations();

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
}

