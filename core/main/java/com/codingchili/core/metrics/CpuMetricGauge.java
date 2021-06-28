package com.codingchili.core.metrics;

import com.codahale.metrics.*;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects CPU metrics using the operating system mx bean.
 */
public class CpuMetricGauge implements MetricSet {
    private final OperatingSystemMXBean os;

    public CpuMetricGauge() {
        os = ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public Map<String, Metric> getMetrics() {
        var metrics = new HashMap<String, Metric>();

        if (os instanceof com.sun.management.OperatingSystemMXBean) {
            var sun = (com.sun.management.OperatingSystemMXBean) os;
            metrics.put("system", (Gauge<Double>) sun::getSystemCpuLoad);
            metrics.put("process", (Gauge<Double>) sun::getProcessCpuLoad);
        } else {
            metrics.put("system", (Gauge<Double>) os::getSystemLoadAverage);
        }
        return metrics;
    }
}
