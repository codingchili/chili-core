package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *
 * "Abstract" benchmark listener.
 */
public class AbstractBenchmarkListener implements BenchmarkListener {

    @Override
    public void onGroupCompleted(BenchmarkGroup group) {
    }

    @Override
    public void onImplementationWarmupComplete(BenchmarkImplementation implementation) {
    }

    @Override
    public void onImplementationTestBegin(BenchmarkImplementation implementation) {
    }

    @Override
    public void onImplementationCompleted(BenchmarkImplementation implementation) {
    }

    @Override
    public void onProgressUpdate(Benchmark benchmark, int iterations) {
    }

    @Override
    public void onBenchmarkCompleted(Benchmark benchmark) {
    }
}
