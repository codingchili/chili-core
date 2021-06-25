package com.codingchili.core.metrics;

import com.codahale.metrics.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.Environment;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.ID_TYPE;

/**
 * Implementation of metric collection backed by micrometer.
 */
public class MetricCollector {
    public static final String PROCESS_NAME = "process.identifier";
    private static final String METRICS_OVERHEAD = "metrics.overhead";
    private final JsonObject metadata = new JsonObject();
    private final MetricRegistry registry;
    private final MetricSettings settings;
    private final CoreContext core;
    private final Logger logger;
    private Timer overhead;

    {
        metadata.put(PROCESS_NAME, String.format("%s/%s",
                ProcessHandle.current().pid(),
                Environment.hostname().orElse("n/a"))
        );
    }

    /**
     * @param core the context to collect metrics from.
     * @param settings settings for the metric collector.
     * @param registryName the name of the metric registry to use.
     */
    public MetricCollector(CoreContext core, MetricSettings settings, String registryName) {
        this.core = core;
        this.settings = settings;
        this.logger = core.logger(getClass());
        this.registry = SharedMetricRegistries.getOrCreate(registryName);

        var timer = TimerSource.of(settings::getRate)
                .setName(CoreStrings.LOG_METRICS);

        core.periodic(timer, handler -> {
            if (settings().isEnabled() && logger != null) {
                snapshot().onSuccess(this::onMetricsSnapshot)
                        .onFailure(logger::onError);
            }
        });
        setupExtraMonitors();
    }

    /**
     * @param metrics log a metrics snapshot.
     */
    public void onMetricsSnapshot(JsonObject metrics) {
        logger.onMetricsSnapshot(metrics);
    }

    /**
     * sets up JVM monitoring based on configuration, for all implementations see:
     * https://github.com/infusionsoft/yammer-metrics/tree/master/metrics-jvm/src/main/java/com/codahale/metrics/jvm
     */
    private void setupExtraMonitors() {
        if (settings.isOverhead()) {
            overhead = registry.timer(METRICS_OVERHEAD);
        }
        settings().getJvm().forEach(jvm -> {
            try {
                var instance = jvm.getMetricImplementation()
                        .getConstructor().newInstance();

                registry.registerAll(jvm.getNamespace(), instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * @return metadata for the metric collector, this data will be included in all metric reports.
     */
    public JsonObject metadata() {
        return metadata;
    }

    /**
     * @param type sets the given type as metadata.
     * @return fluent.
     */
    public MetricCollector type(String type) {
        metadata.put(ID_TYPE, type);
        return this;
    }

    /**
     * @return the settings for this collector.
     */
    public MetricSettings settings() {
        return settings;
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
     * @return a generated json report with the target fields.
     */
    public Future<JsonObject> snapshot() {
        var promise = Promise.<JsonObject>promise();

        core.blocking(blocking -> {
            var reporter = (Runnable) () -> {
                var filters = settings.getFilters();
                var json = new JsonObject();

                registry.getMetrics().forEach((key, value) -> {
                    if (filters.isEmpty()) {
                        json.mergeIn(settings.filter(MetricFilter.convert(value, key)));
                    } else {
                        filters.stream()
                                .filter(filter -> key.startsWith(filter.getPath()))
                                .findFirst()
                                .ifPresent(filter -> json.mergeIn(filter.apply(value, key)));
                    }
                });
                json.mergeIn(metadata);
                blocking.complete(json);
            };

            if (settings.isOverhead()) {
                overhead.time(reporter);
            } else {
                reporter.run();
            }
        }, promise);
        return promise.future();
    }
}
