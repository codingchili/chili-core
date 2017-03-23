package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *         <p>
 *         Listens for progress during benchmarking.
 */
public interface BenchmarkListener {

    /**
     * Triggers when a group of implementations has completed.
     *
     * @param group the group that was completed.
     */
    void onGroupCompleted(BenchmarkGroup group);

    /**
     * Triggers when an implementation has completed the warmup phase.
     *
     * @param implementation the benchmark implementation that was warmed up.
     */
    void onImplementationWarmupComplete(BenchmarkImplementation implementation);

    /**
     * Triggers when an implementation starts the actual testing.
     *
     * @param implementation the implementation under test.
     */
    void onImplementationTestBegin(BenchmarkImplementation implementation);

    /**
     * Triggers when an implementation has completed testing.
     *
     * @param implementation the benchmark implementation that was completed.
     */
    void onImplementationCompleted(BenchmarkImplementation implementation);

    /**
     * Triggers in intervals of 5% when iterations has completed.
     *
     * @param benchmark  the benchmark that was updated
     * @param iterations the number of iterations that have been completed
     */
    void onProgressUpdate(Benchmark benchmark, int iterations);

    /**
     * Triggers when a single benchmark is completed.
     *
     * @param benchmark the benchmark that was completed.
     */
    void onBenchmarkCompleted(Benchmark benchmark);
}
