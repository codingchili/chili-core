package com.codingchili.core.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects information about the operating system.
 */
public class OsMetricGauge implements MetricSet {
    private OperatingSystemMXBean os;

    public OsMetricGauge() {
        os = ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public Map<String, Metric> getMetrics() {
        var metrics = new HashMap<String, Metric>();
        metrics.put("os.processors", (Gauge<Integer>) os::getAvailableProcessors);
        metrics.put("os.name", (Gauge<String>) os::getName);
        metrics.put("os.arch", (Gauge<String>) os::getArch);
        metrics.put("os.version", (Gauge<String>) os::getVersion);
        return metrics;
    }
}
