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
    private ConsoleLogger logger = new ConsoleLogger();

    @Override
    public void onGroupStarted(BenchmarkGroup group) {
        log(getBenchmarkGroupStarted(group));
    }

    @Override
    public void onGroupCompleted(BenchmarkGroup group) {
        log(getBenchmarkGroupCompleted(group));
    }

    @Override
    public void onImplementationWarmup(BenchmarkImplementation implementation) {
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
        log(getBenchmarkProgressUpdate(benchmark, progressAsPercent(benchmark, iterations)));
    }

    @Override
    public void onBenchmarkCompleted(Benchmark benchmark) {
        log(getBenchmarkCompleted(benchmark));
    }

    private String progressAsPercent(Benchmark benchmark, int iterations) {
        return formatAsPercent((iterations * 1.0 / benchmark.getIterations()) * 100);
    }

    private void log(String message) {
        logger.log(logger.event(LOG_BENCHMARK, Level.INFO)
                .put(ID_MESSAGE, message));
    }
}
