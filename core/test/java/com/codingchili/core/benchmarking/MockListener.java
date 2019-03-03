package com.codingchili.core.benchmarking;

import io.vertx.ext.unit.TestContext;

/**
 * Asserts that all test methods of a listener is called and that
 * they are called in the expected order.
 */
public class MockListener implements BenchmarkListener {
    private TestContext test;
    private boolean isGroupStarted = false;
    private boolean isGroupCompleted = false;
    private boolean isImplementationWarmupStarted = false;
    private boolean isImplementationWarmupComplete = false;
    private boolean isImplementationTestBegin = false;
    private boolean isImplementationCompleted = false;
    private boolean isProgressUpdated = false;
    private boolean isBenchmarkCompleted = false;

    /**
     * @param test uses a testcontext for assertions.
     */
    public MockListener(TestContext test) {
        this.test = test;
    }

    public void assertAllEventsTriggered() {
        test.assertTrue(isGroupStarted && isGroupCompleted && isImplementationWarmupStarted &&
                isImplementationWarmupComplete && isImplementationTestBegin && isImplementationCompleted &&
                isProgressUpdated && isBenchmarkCompleted, "all listeners was not activated.");
    }

    @Override
    public void onGroupStarted(BenchmarkGroup group) {
        isGroupStarted = true;
    }

    @Override
    public void onGroupCompleted(BenchmarkGroup group) {
        test.assertTrue(isGroupStarted, "group completed before it was started");
        isGroupCompleted = true;
    }

    @Override
    public void onImplementationWarmup(BenchmarkImplementation implementation) {
        test.assertTrue(isGroupStarted, "implementation warmup before group started");
        isImplementationWarmupStarted = true;
    }

    @Override
    public void onImplementationWarmupComplete(BenchmarkImplementation implementation) {
        test.assertTrue(isImplementationWarmupStarted, "warmup completed before started");
        isImplementationWarmupComplete = true;
    }

    @Override
    public void onImplementationTestBegin(BenchmarkImplementation implementation) {
        isImplementationTestBegin = true;
        test.assertTrue(isImplementationWarmupComplete, "test begin before warmup complete");
    }

    @Override
    public void onImplementationCompleted(BenchmarkImplementation implementation) {
        isImplementationCompleted = true;
        test.assertTrue(isImplementationTestBegin, "implementation completed before testing begun");
    }

    @Override
    public void onProgressUpdate(Benchmark benchmark, int iterations) {
        isProgressUpdated = true;
        test.assertTrue(isImplementationWarmupStarted, "progress was made before warmup started");
    }

    @Override
    public void onBenchmarkCompleted(Benchmark benchmark) {
        isBenchmarkCompleted = true;
        test.assertTrue(isImplementationTestBegin, "benchmark completed before testing begun");
    }
}
