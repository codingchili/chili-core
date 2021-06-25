package com.codingchili.core.metrics;

import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.*;

/**
 * Maps implementations used to collect jvm/process/os metrics.
 */
public enum JvmMetric {
    threads(ThreadStateGauge.class, "threads"),
    classes(ClassLoadingGaugeSet.class, "classes"),
    gc(GarbageCollectorMetricSet.class, "gc"),
    memory(MemoryUsageGaugeSet.class, "memory"),
    jvm(JvmAttributeGaugeSet.class, "jvm"),
    cpu(CpuMetricGauge.class, "cpu"),
    process(ProcessMetrics.class, "process"),
    os(OsMetricGauge.class, "os");

    private Class<MetricSet> impl;
    private String namespace;

    /**
     * @return the namespace which the metric registers under.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the metric implementation class.
     */
    public Class<MetricSet> getMetricImplementation() {
        return impl;
    }

    @SuppressWarnings("unchecked")
    JvmMetric(Class<? extends MetricSet> implClass, String namespace) {
        this.impl = (Class<MetricSet>) implClass;
        this.namespace = namespace;
    }
}
