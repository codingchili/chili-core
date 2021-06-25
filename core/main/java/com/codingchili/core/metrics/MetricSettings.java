package com.codingchili.core.metrics;

import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

import java.util.*;
import java.util.function.Consumer;

import com.codingchili.core.files.Configurations;

/**
 * Settings for the generation of metrics.
 */
public class MetricSettings {
    public static final String REGISTRY_NAME = "default";
    private Set<String> includes = MetricFilter.defaultIncludes();
    private List<MetricFilter> filters = new ArrayList<>();
    private List<JvmMetric> jvm = new ArrayList<>();
    private boolean overhead;
    private boolean enabled = false;
    private int rate = 15000;

    /**
     * @return true if metrics should be collected.
     */
    public boolean isEnabled() {
        return enabled;
    }

    public MetricSettings setEnabled(boolean enabled) {
        this.enabled = enabled;
        // update vertx options.
        Configurations.system().getOptions()
                .getMetricsOptions()
                .setEnabled(enabled);
        return this;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    /**
     * @param includes a list of default includes for the filters.
     * @return fluent
     */
    public MetricSettings setIncludes(Set<String> includes) {
        this.includes = includes;
        return this;
    }

    /**
     * @param include a property to include for matching metrics.
     * @return fluent.
     */
    public MetricSettings include(String include) {
        this.includes.add(include);
        return this;
    }

    /**
     * @return the rate in ms at which metrics are collected.
     */
    public int getRate() {
        return rate;
    }

    /**
     * @param rate in milliseconds.
     * @return fluent.
     */
    public MetricSettings setRate(int rate) {
        this.rate = rate;
        return this;
    }

    /**
     * @return a list of filters used to filter out performance metrics to greatly
     * reduce the overhead of collection and indexing. If empty nothing will be
     * filtered.
     */
    public List<MetricFilter> getFilters() {
        return filters;
    }

    public MetricSettings setFilters(List<MetricFilter> filters) {
        this.filters = filters;
        return this;
    }

    /**
     * Adds a new filter to the metric configuration.
     *
     * @param consumer the configurator.
     * @return fluent.
     */
    public MetricSettings addFilter(Consumer<MetricFilter> consumer) {
        var filter = new MetricFilter();
        filter.getInclude().addAll(includes);
        consumer.accept(filter);
        filters.add(filter);
        return this;
    }

    public boolean isOverhead() {
        return overhead;
    }

    /**
     * @param overhead true if reporting overhead should be recorded.
     * @return fluent
     */
    public MetricSettings setOverhead(boolean overhead) {
        this.overhead = overhead;
        return this;
    }

    /**
     * enables all jvm performance metrics.
     *
     * @return fluent.
     */
    public MetricSettings allJvm() {
        jvm.addAll(Arrays.asList(JvmMetric.values()));
        return this;
    }

    public List<JvmMetric> getJvm() {
        return jvm;
    }

    /**
     * @param jvm a set of jvm metrics to enable.
     * @return fluent.
     */
    public MetricSettings setJvm(List<JvmMetric> jvm) {
        this.jvm = jvm;
        return this;
    }

    /**
     * @param metrics a list of jvm metrics to enable.
     * @return fluent.
     */
    public MetricSettings jvm(JvmMetric... metrics) {
        Collections.addAll(jvm, metrics);
        return this;
    }

    /**
     * @return metrics options which are passed to vertx.
     */
    public MetricsOptions createVertxOptions() {
        return new DropwizardMetricsOptions()
                .setRegistryName(REGISTRY_NAME)
                .setEnabled(enabled);
    }

    public JsonObject filter(JsonObject json) {
        if (!includes.isEmpty()) {
            var fields = json.fieldNames();
            for (var field : fields) {
                JsonObject entry = json.getJsonObject(field);
                var iterator = entry.iterator();
                while (iterator.hasNext()) {
                    if (!includes.contains(iterator.next().getKey())) {
                        iterator.remove();
                    }
                }
            }
        }
        return json;
    }
}
