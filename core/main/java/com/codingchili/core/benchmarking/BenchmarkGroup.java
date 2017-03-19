package com.codingchili.core.benchmarking;

import java.util.List;

/**
 * @author Robin Duda
 *         <p>
 *         Contains a set of benchmarks and groups them by type, for example
 *         "network" or "storage".
 */
public interface BenchmarkGroup
{
    /**
     * The name of the benchmark group, for example "networking" or "storage".
     * Benchmarks are grouped under this in the reports.
     *
     * @return a string that identifies a group of benchmarks.
     */
    String groupName();

    /**
     * Get the implementations enlisted for benchmarking in this group.
     *
     * @return a list of implementations to benchmark.
     */
    List<BenchmarkImplementation> implementations();

    /**
     * Adds a new benchmark to the benchmark group.
     *
     * @param benchmark the benchmark to be added.
     * @return fluent
     */
    BenchmarkGroup add(BenchmarkImplementation benchmark);
}
