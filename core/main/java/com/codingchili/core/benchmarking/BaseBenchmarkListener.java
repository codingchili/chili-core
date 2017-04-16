package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *         <p>
 *         "Abstract" benchmark listener.
 */
public class BaseBenchmarkListener implements BenchmarkListener {

    @Override
    public void onGroupStarted(BenchmarkGroup group) {
    }

    @Override
    public void onGroupCompleted(BenchmarkGroup group) {
    }

    @Override
    public void onImplementationWarmup(BenchmarkImplementation implementation) {
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
