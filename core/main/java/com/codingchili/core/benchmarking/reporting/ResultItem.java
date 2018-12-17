package com.codingchili.core.benchmarking.reporting;

import com.codingchili.core.benchmarking.Benchmark;

/**
 * A resultset contains one result item for each implementation.
 */
public class ResultItem {
    private String timeFormatted;
    private String rateFormatted;
    private String name;
    private Integer rate;
    private float localIndex;

    public ResultItem(Benchmark benchmark) {
        this.name = benchmark.getName();
        this.timeFormatted = benchmark.getTimeFormatted();
        this.rateFormatted = benchmark.getRateFormatted();
        this.rate = benchmark.getRate();
    }

    public void setLocalIndex(float v) {
        this.localIndex = v;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }

    public String getRateFormatted() {
        return rateFormatted;
    }

    public String getName() {
        return name;
    }

    public Integer getRate() {
        return rate;
    }

    public float getLocalIndex() {
        return localIndex;
    }
}