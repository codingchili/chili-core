package com.codingchili.core.metrics;

import com.codahale.metrics.*;

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
        metrics.put("processors", (Gauge<Integer>) os::getAvailableProcessors);
        metrics.put("name", (Gauge<String>) os::getName);
        metrics.put("arch", (Gauge<String>) os::getArch);
        metrics.put("version", (Gauge<String>) os::getVersion);
        return metrics;
    }
}
