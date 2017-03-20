package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *         <p>
 *         Implemented by executable benchmarks.
 */
public interface Benchmark {
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
    String name();

    /**
     * @return the name of the group the benchmark belongs to.
     */
    String group();

    /**
     * @return the name of the implementation the benchmark was executed for.
     */
    String implementation();

    /**
     * Number of iterations that the benchmark operation is to be executed.
     *
     * @return a integer indicating the number of operation iterations.
     */
    int iterations();

    /**
     * Indicates how many operations may be queued at any one given time.
     * Higher parallellism is recommended for tests that executes with a known
     * latency, for example a test that accesses a remote database or reads from disc.
     *
     * @return the number of operations that may be queued at the given time.
     */
    int parallelism();
}
