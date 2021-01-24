package com.codingchili.core.configuration.system;

import com.codingchili.core.files.Configurations;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings for the generation of metrics.
 */
public class MetricSettings {
    public static final String REGISTRY_NAME = "default";
    private List<MetricFilter> filters = new ArrayList<>();
    private boolean classloader;
    private boolean memory;
    private boolean gc;
    private boolean threads;
    private boolean cpu;
    private boolean jvm;
    private boolean enabled = false;
    private int rate = 15000;
    private List<String> includes = List.of("value",
            "count",
            "mean",
            "min",
            "max",
            "median",
            "oneMinuteRate",
            "fiveMinuteRate",
            "95%",
            "99.9%");

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

    public List<String> getIncludes() {
        return includes;
    }

    /**
     * @param includes a list of default includes for the filters.
     */
    public void setIncludes(List<String> includes) {
        this.includes = includes;
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

    public boolean isClassloader() {
        return classloader;
    }

    /**
     * @param classloader true if metrics are to be collected
     * @return fluent.
     */
    public MetricSettings setClassloader(boolean classloader) {
        this.classloader = classloader;
        return this;
    }

    public boolean isMemory() {
        return memory;
    }

    /**
     * @param memory true if metrics are to be collected
     * @return fluent.
     */
    public MetricSettings setMemory(boolean memory) {
        this.memory = memory;
        return this;
    }

    public boolean isGc() {
        return gc;
    }

    /**
     * @param gc true if metrics are to be collected
     * @return fluent.
     */
    public MetricSettings setGc(boolean gc) {
        this.gc = gc;
        return this;
    }

    /**
     * @param enabled true if metrics are to be collected.
     * @return fluent.
     */
    public MetricSettings setJvmAttributes(boolean enabled) {
        this.jvm = enabled;
        return this;
    }

    public boolean isJvmAttributes() {
        return jvm;
    }

    public boolean isThreads() {
        return threads;
    }

    /**
     * @param threads true if metrics are to be collected
     * @return fluent.
     */
    public MetricSettings setThreads(boolean threads) {
        this.threads = threads;
        return this;
    }

    public boolean isCpu() {
        return cpu;
    }

    /**
     * @param enabled true if metris are to be collected.
     * @return fluent.
     */
    public MetricSettings setCpu(boolean enabled) {
        this.cpu = enabled;
        return this;
    }

    /**
     * enables all jvm performance metrics.
     *
     * @return fluent.
     */
    public MetricSettings allJvm() {
        threads = true;
        jvm = true;
        gc = true;
        classloader = true;
        memory = true;
        cpu = true;
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
