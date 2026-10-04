package com.codingchili.core.benchmarking.reporting;

import com.codingchili.core.benchmarking.Benchmark;

/**
 * The result of one benchmark (operation) for one implementation.
 */
public class ResultItem {
    /**
     * Number of shades used to bucket results relative to the fastest implementation.
     */
    public static final int HEAT_STEPS = 5;
    private final String timeFormatted;
    private final String rateFormatted;
    private final String implementation;
    private final String name;
    private final long elapsedNanos;
    private final int rate;
    private int percentOfFastest;
    private boolean fastest;

    /**
     * @param implementation the name of the implementation that was benchmarked.
     * @param benchmark      the finished benchmark.
     */
    public ResultItem(String implementation, Benchmark benchmark) {
        this.implementation = implementation;
        this.name = benchmark.getName();
        this.timeFormatted = benchmark.getTimeFormatted();
        this.rateFormatted = benchmark.getRateFormatted();
        this.elapsedNanos = benchmark.getElapsedNanos();
        this.rate = benchmark.getRate();
    }

    /**
     * Compares this result to the fastest result of the same operation.
     *
     * @param fastestRate the highest rate of all implementations for the same operation.
     * @return fluent
     */
    public ResultItem compareTo(int fastestRate) {
        this.fastest = (rate == fastestRate);
        this.percentOfFastest = (fastestRate == 0) ? 0 : (int) Math.round(rate * 100.0 / fastestRate);
        return this;
    }

    /**
     * @return the name of the operation (benchmark).
     */
    public String getName() {
        return name;
    }

    /**
     * @return the name of the implementation.
     */
    public String getImplementation() {
        return implementation;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }

    public String getRateFormatted() {
        return rateFormatted;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public int getRate() {
        return rate;
    }

    /**
     * @return the rate as a percentage of the fastest implementation for the same operation.
     */
    public int getPercentOfFastest() {
        return percentOfFastest;
    }

    /**
     * @return the bar width in percent, small results keep a minimum width to remain visible.
     */
    public int getBarWidth() {
        return Math.max(1, percentOfFastest);
    }

    /**
     * @return 1-{@link #HEAT_STEPS}, the shade used for the result relative to the fastest.
     */
    public int getHeat() {
        if (percentOfFastest >= 90) {
            return 5;
        } else if (percentOfFastest >= 70) {
            return 4;
        } else if (percentOfFastest >= 45) {
            return 3;
        } else if (percentOfFastest >= 20) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * @return true if this is the fastest implementation of the operation.
     */
    public boolean isFastest() {
        return fastest;
    }
}
