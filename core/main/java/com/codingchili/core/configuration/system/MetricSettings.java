package com.codingchili.core.configuration.system;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings for the generation of metrics.
 */
public class MetricSettings {
    private List<MetricFilter> filters = new ArrayList<>();
    private boolean enabled = false;
    private int rate = 15000;

    /**
     * @return true if metrics should be collected.
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the rate in ms at which metrics are collected.
     */
    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * @return a list of filters used to filter out performance metrics to greatly
     * reduce the overhead of collection and indexing. If empty nothing will be
     * filtered.
     */
    public List<MetricFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MetricFilter> filters) {
        this.filters = filters;
    }
}
