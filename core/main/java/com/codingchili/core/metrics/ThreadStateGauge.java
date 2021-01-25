package com.codingchili.core.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread state gauge with deadlock detection removed as it may return
 * empty set which is not supported by the json serializer - vertx.dropwizard/Helper.
 */
public class ThreadStateGauge extends ThreadStatesGaugeSet {
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
