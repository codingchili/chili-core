package com.codingchili.core.benchmarking;

import java.util.List;

/**
 * @author Robin Duda
 *         <p>
 *         Contains a set of benchmarks and groups them by type, for example
 *         "network" or "storage".
 */
public interface BenchmarkGroup {
    /**
     * The name of the benchmark group, for example "networking" or "storage".
     * Benchmarks are grouped under this in the reports.
     *
     * @return a string that identifies a group of benchmarks.
     */
    String getName();

    /**
     * Number of iterations that the benchmark operation is to be executed.
     *
     * @return a integer indicating the number of operation iterations.
     */
    int getIterations();

    /**
     * Get the implementations enlisted for benchmarking in this group.
     *
     * @return a list of implementations to benchmark.
     */
    List<BenchmarkImplementation> getImplementations();

    /**
     * Sets the implementations in the group to the given list. May be used when creating reports.
     *
     * @param implementations the implementations to set.
     * @return fluent
     */
    BenchmarkGroup setImplementations(List<BenchmarkImplementation> implementations);

    /**
     * Adds a new benchmark to the benchmark group.
     *
     * @param benchmark the benchmark to be added.
     * @return fluent
     */
    BenchmarkGroup add(BenchmarkImplementation benchmark);

    /**
     * Return the number of iterations to complete before calling a listeners
     * progress listener.
     *
     * @return the number of iterations.
     */
    int getProgressInterval();
}
