package com.codingchili.core.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps implementations used to collect jvm/process/os metrics.
 */
public enum JvmMetric {
    threads(ThreadStatesGaugeSetNoDeadlock.class),
    classes(ClassLoadingGaugeSet.class),
    gc(GarbageCollectorMetricSet.class),
    memory(MemoryUsageGaugeSet.class),
    jvm(JvmAttributeGaugeSet.class),
    cpu(CpuMetricGauge.class),
    process(ProcessMetrics.class),
    os(OsMetricGauge.class);

    private Class<MetricSet> impl;

    public Class<MetricSet> getMetricImplementation() {
        return impl;
    }

    @SuppressWarnings("unchecked")
    JvmMetric(Class<? extends MetricSet> implClass) {
        this.impl = (Class<MetricSet>) implClass;
    }

    static class ThreadStatesGaugeSetNoDeadlock extends ThreadStatesGaugeSet {
        private static final String THREAD_MONITOR_DEADLOCK_KEY = "deadlocks";

        @Override
        public Map<String, Metric> getMetrics() {
            var current = super.getMetrics();
            var filtered = new HashMap<String, Metric>();
            // deadlocks returns an emptySet which cannot be converted
            // to json in vertx-dropwizard-metrics/helper.
            current.forEach((key, value) -> {
                if (!key.equals(THREAD_MONITOR_DEADLOCK_KEY)) {
                    filtered.put(key, value);
                }
            });
            return filtered;
        }
    }
}
