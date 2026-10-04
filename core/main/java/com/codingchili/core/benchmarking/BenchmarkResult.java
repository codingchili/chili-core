package com.codingchili.core.benchmarking;

/**
 * Results for benchmarks.
 */
public interface BenchmarkResult {

    /**
     * @return the elapsed time formatted with a unit, for example "12.4 ms" or "1.52 s".
     */
    String getTimeFormatted();

    /**
     * @return the number of operations per second as a formatted string.
     */
    String getRateFormatted();

    /**
     * @return the number of operations per second.
     */
    int getRate();

    /**
     * Formats a duration with a unit that keeps 3 significant digits.
     *
     * @param nanos the duration in nanoseconds.
     * @return the formatted duration, for example "850 µs", "12.4 ms" or "1.52 s".
     */
    static String formatNanos(long nanos) {
        if (nanos < 1_000) {
            return nanos + " ns";
        } else if (nanos < 1_000_000) {
            return significant(nanos / 1_000.0) + " µs";
        } else if (nanos < 1_000_000_000) {
            return significant(nanos / 1_000_000.0) + " ms";
        } else {
            return significant(nanos / 1_000_000_000.0) + " s";
        }
    }

    private static String significant(double value) {
        if (value >= 100) {
            return String.format(java.util.Locale.ROOT, "%.0f", value);
        } else if (value >= 10) {
            return String.format(java.util.Locale.ROOT, "%.1f", value);
        } else {
            return String.format(java.util.Locale.ROOT, "%.2f", value);
        }
    }
}
