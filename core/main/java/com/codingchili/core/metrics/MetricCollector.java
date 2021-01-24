package com.codingchili.core.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.jvm.*;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.MetricSettings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

/**
 * Implementation of metric collection backed by micrometer.
 */
public class MetricCollector {
    private static final String METRICS_OVERHEAD = "metrics.overhead";
    private static final String THREAD_MONITOR_DEADLOCK_KEY = "deadlocks";
    private final MetricsService service;
    private final MetricRegistry registry;
    private final CoreContext core;
    private final Timer overhead;
    private final Logger logger;

    /**
     * @param core the context to collect metrics from.
     */
    public MetricCollector(CoreContext core) {
        this.core = core;
        logger = core.logger(getClass());
        service = MetricsService.create(core.vertx());
        registry = SharedMetricRegistries.getOrCreate(MetricSettings.REGISTRY_NAME);
        overhead = registry.timer(METRICS_OVERHEAD);

        var timer = TimerSource.of(this::getMetricTimer)
                .setName(CoreStrings.LOG_METRICS);

        core.periodic(timer, handler -> {
            if (settings().isEnabled() && logger != null) {
                report(settings())
                        .onSuccess(this::onMetricsSnapshot)
                        .onFailure(logger::onError);
            }
        });
        setupJvmMonitoring();
    }

    public void onMetricsSnapshot(JsonObject metrics) {
        logger.onMetricsSnapshot(metrics);
    }

    /**
     * sets up JVM monitoring based on configuration, for all implementations see:
     * https://github.com/infusionsoft/yammer-metrics/tree/master/metrics-jvm/src/main/java/com/codahale/metrics/jvm
     */
    private void setupJvmMonitoring() {
        var settings = settings();
        var measurements = new LinkedBlockingQueue<>(List.of(
                settings.isThreads(),
                settings.isClassloader(),
                settings.isGc(),
                settings.isMemory(),
                settings.isJvmAttributes(),
                settings.isCpu()
        ));
        Stream.of(
                threadMonitorNoDeadlock(),
                new ClassLoadingGaugeSet(),
                new GarbageCollectorMetricSet(),
                new MemoryUsageGaugeSet(),
                new JvmAttributeGaugeSet(),
                new CpuMetricGauge()
        ).filter(metric -> measurements.poll())
                .forEach(registry::registerAll);
    }

    private MetricSet threadMonitorNoDeadlock() {
        return new ThreadStatesGaugeSet() {
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
        };
    }

    /**
     * @return the settings for this collector.
     */
    public MetricSettings settings() {
        return Configurations.system().getMetrics();
    }

    /**
     * @return the underlying service implementation.
     */
    public MetricsService service() {
        return service;
    }

    /**
     * @return the metrics registry used to collect metrics. Can be used
     * to add custom generators.
     */
    public MetricRegistry registry() {
        return registry;
    }

    /**
     * Produces a report of the current metric snapshot.
     *
     * @param settings the settings to apply when generating the report.
     * @return a generated json report with the target fields.
     */
    public Future<JsonObject> report(MetricSettings settings) {
        var filters = settings.getFilters();
        var promise = Promise.<JsonObject>promise();

        core.blocking(done -> overhead.time(() -> {
            if (filters.isEmpty()) {
                var report = service.getMetricsSnapshot("");
                done.complete(settings.filter(report));
            } else {
                final JsonObject root = new JsonObject();
                filters.forEach(filter -> {
                    var capture = service.getMetricsSnapshot(filter.getPath());
                    filter.apply(capture, root);
                });
                done.complete(root);
            }
        }), promise);
        return promise.future();
    }

    private int getMetricTimer() {
        return settings().getRate();
    }
}
