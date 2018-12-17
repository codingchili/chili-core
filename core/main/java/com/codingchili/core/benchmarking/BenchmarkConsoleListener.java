package com.codingchili.core.benchmarking;

import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Captures events from the logging executor and logs to console.
 */
public class BenchmarkConsoleListener implements BenchmarkListener {
    private ConsoleLogger logger = new ConsoleLogger(getClass());
    private BenchmarkGroup group;
    private BenchmarkImplementation implementation;

    @Override
    public void onGroupStarted(BenchmarkGroup group) {
        log(getBenchmarkGroupStarted(group));
        this.group = group;
    }

    @Override
    public void onGroupCompleted(BenchmarkGroup group) {
        log(getBenchmarkGroupCompleted(group));
    }

    @Override
    public void onImplementationWarmup(BenchmarkImplementation implementation) {
        this.implementation = implementation;
        log(getBenchmarkImplementationWarmup(implementation));
    }

    @Override
    public void onImplementationWarmupComplete(BenchmarkImplementation implementation) {
        log(getBenchmarkImplementationWarmupComplete(implementation));
    }

    @Override
    public void onImplementationTestBegin(BenchmarkImplementation implementation) {
        log(getBenchmarkImplementationTestBegin(implementation));
    }

    @Override
    public void onImplementationCompleted(BenchmarkImplementation implementation) {
        log(getBenchmarkImplementationComplete(implementation));
    }

    @Override
    public void onProgressUpdate(Benchmark benchmark, int iterations) {
        log(getBenchmarkProgressUpdate(implementation, benchmark, progressAsPercent(iterations)));
    }

    @Override
    public void onBenchmarkCompleted(Benchmark benchmark) {
        log(getBenchmarkCompleted(implementation, benchmark));
    }

    private String progressAsPercent(int iterations) {
        return formatAsPercent((iterations * 1.0 / group.getIterations()) * 100);
    }

    private void log(String message) {
        logger.event(LOG_BENCHMARK, Level.INFO).send(message);
    }
}
