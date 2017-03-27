package com.codingchili.core.benchmarking;

import static com.codingchili.core.configuration.CoreStrings.ID_MESSAGE;

import java.text.DecimalFormat;

import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

/**
 * @author Robin Duda
 *         <p>
 *         Captures events from the logging executor and logs to console.
 */
public class BenchmarkConsoleListener implements BenchmarkListener {
    private ConsoleLogger logger = new ConsoleLogger();

    @Override
    public void onGroupStarted(BenchmarkGroup group) {
        log("Starting group " + group.getName());
    }

    @Override
    public void onGroupCompleted(BenchmarkGroup group) {
        log("Completed group " + group.getName());
    }

    @Override
    public void onImplementationWarmup(BenchmarkImplementation implementation) {
        log("Warmup started for " + implementation.getName());
    }

    @Override
    public void onImplementationWarmupComplete(BenchmarkImplementation implementation) {
        log("Warmup completed for " + implementation.getName());
    }

    @Override
    public void onImplementationTestBegin(BenchmarkImplementation implementation) {
        log("Starting tests for " + implementation.getName());
    }

    @Override
    public void onImplementationCompleted(BenchmarkImplementation implementation) {
        log("Tests completed for " + implementation.getName());
    }

    @Override
    public void onProgressUpdate(Benchmark benchmark, int iterations) {
        log("Tests for " + benchmark.getImplementation() + "::" + benchmark.getName() +
                " " + progressAsPercent(benchmark, iterations) + "%");
    }

    @Override
    public void onBenchmarkCompleted(Benchmark benchmark) {
        log("Completed benchmark " + benchmark.getImplementation() +
                "::" + benchmark.getName() + " in " + benchmark.getElapsedMS() + " ms.");
    }

    private String progressAsPercent(Benchmark benchmark, int iterations) {
        return new DecimalFormat("#.00")
                .format((iterations * 1.0 / benchmark.getIterations()) * 100);
    }

    private void log(String message) {
        logger.log(logger.event("[BENCHMARKING]", Level.INFO)
                .put(ID_MESSAGE, message));
    }
}
