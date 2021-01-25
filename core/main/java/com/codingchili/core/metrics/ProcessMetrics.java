package com.codingchili.core.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about the currently running process.
 */
public class ProcessMetrics implements MetricSet {
    private RuntimeMXBean runtime;

    public ProcessMetrics() {
        runtime = ManagementFactory.getRuntimeMXBean();
    }

    @Override
    public Map<String, Metric> getMetrics() {
        var metrics = new HashMap<String, Metric>();
        metrics.put("process.pid", (Gauge<Long>) runtime::getPid);
        metrics.put("process.name", (Gauge<String>) runtime::getName);
        metrics.put("process.jvm", (Gauge<String>) runtime::getVmName);
        metrics.put("process.uptime", (Gauge<Long>) runtime::getUptime);
        return metrics;
    }
}
