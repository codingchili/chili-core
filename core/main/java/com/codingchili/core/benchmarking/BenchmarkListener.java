package com.codingchili.core.benchmarking;

/**
 * Listens for progress during benchmarking.
 */
public interface BenchmarkListener {

    /**
     * Triggers when a group is starting benchmarking.
     *
     * @param group the group that is starting up.
     */
    default void onGroupStarted(BenchmarkGroup group) {
    }

    /**
     * Triggers when a group of implementations has completed.
     *
     * @param group the group that was completed.
     */
    default void onGroupCompleted(BenchmarkGroup group) {
    }

    /**
     * Triggers when an implementation is starting to warmup.
     *
     * @param implementation the implementation to warm up.
     */
    default void onImplementationWarmup(BenchmarkImplementation implementation) {
    }

    /**
     * Triggers when an implementation has completed the warmup phase.
     *
     * @param implementation the benchmark implementation that was warmed up.
     */
    default void onImplementationWarmupComplete(BenchmarkImplementation implementation) {
    }

    /**
     * Triggers when an implementation starts the actual testing.
     *
     * @param implementation the implementation under test.
     */
    default void onImplementationTestBegin(BenchmarkImplementation implementation) {
    }

    /**
     * Triggers when an implementation has completed testing.
     *
     * @param implementation the benchmark implementation that was completed.
     */
    default void onImplementationCompleted(BenchmarkImplementation implementation) {
    }

    /**
     * Triggers in intervals of 5% when iterations has completed.
     *
     * @param benchmark  the benchmark that was updated
     * @param iterations the number of iterations that have been completed
     */
    default void onProgressUpdate(Benchmark benchmark, int iterations) {
    }

    /**
     * Triggers when a single benchmark is completed.
     *
     * @param benchmark the benchmark that was completed.
     */
    default void onBenchmarkCompleted(Benchmark benchmark) {
    }
}
