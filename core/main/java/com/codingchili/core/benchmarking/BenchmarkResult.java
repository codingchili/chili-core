package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 * <p>
 * Results for benchmarks.
 */
public interface BenchmarkResult {
    int EPOCH_BASE = 3600000;
    String DATE_FORMAT = "HH:mm:ss.SSS";

    /**
     * @return time formatted as HH:mm:ss.SSS using the elapsed ms as source.
     */
    String getTimeFormatted();

    /**
     * @return the number of defaults per second as a formatted string.
     */
    String getRateFormatted();

    /**
     * @return the number of defaults per second.
     */
    int getRate();


}
