package com.codingchili.core.metrics;

import com.codahale.metrics.Metric;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.impl.Helper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Configuration used to filter metrics.
 */
public class MetricFilter {
    private Set<String> include = defaultIncludes();
    private Set<String> exclude = new HashSet<>();
    private String path;
    private String alias;

    /**
     * @return the path to the metric, for example 'vertx.eventbus.handlers'.
     */
    public String getPath() {
        return path;
    }

    public MetricFilter setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * @return the alias to write the metrics as, for example if the path is "vertx.eventbus.handlers"
     * an alias could be used in events, such as {myAlias: ... metrics} especially useful for longer paths
     * such as vertx.http.servers.0.0.0.0:8080.requests which could be aliased as "myService1.requests".
     * <p>
     * Note: the alias can only be applied if the filter matches exactly one metric.
     */
    public String getAlias() {
        return alias;
    }

    @JsonIgnore
    public String aliasOrPath() {
        return (alias == null) ? path : alias;
    }

    public MetricFilter setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * @return a list of metrics to include from the event, some metric types contain a large number
     * of fields, all of them might not be relevant. Specifying include lists greatly reduces the
     * size of the events.
     */
    public Set<String> getInclude() {
        return include;
    }

    public void setInclude(Set<String> include) {
        this.include = include;
    }

    public Set<String> getExclude() {
        return exclude;
    }

    /**
     * @param exclude a list of keys to ignore from metric events matching the filter.
     */
    public void setExclude(Set<String> exclude) {
        this.exclude = exclude;
    }

    /**
     * @param key the key to include from entries matching the filter.
     * @return fluent.
     */
    public MetricFilter include(String... key) {
        include.addAll(Arrays.asList(key));
        return this;
    }

    /**
     * @param key the key to exclude from entries matching the filter.
     * @return fluent.
     */
    public MetricFilter exclude(String... key) {
        exclude.addAll(Arrays.asList(key));
        return this;
    }

    /**
     * Applies the filtering logic to a metrics object.
     *
     * @param metric converted to json and filtered.
     * @param path   the full path of the metrics object.
     * @return a converted metrics object with filtered properties.
     */
    public JsonObject apply(Metric metric, String path) {
        var capture = Helper.convertMetric(metric, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);

        // iterate over individual metric objects.
        var iterator = capture.iterator();

        // filter fields in each metric.
        while (iterator.hasNext()) {
            var next = iterator.next();
            var key = next.getKey();

            if (exclude.contains(key) || !include.contains(key)) {
                iterator.remove();
            }
        }
        return new JsonObject().put(
                (alias != null) ? path.replace(this.path, alias) : path,
                capture
        );
    }

    /**
     * @param metric a metric to convert to a json object.
     * @param path   the path to the metric, used as the key for the data.
     * @return a json object where the path is mapped to an object containing the metric data.
     */
    public static JsonObject convert(Metric metric, String path) {
        var capture = Helper.convertMetric(metric, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
        return new JsonObject().put(path, capture);
    }

    /**
     * @return a list of default includes for dropwizard.
     * Use includes and excludes to modify this with filters.
     */
    public static Set<String> defaultIncludes() {
        return new HashSet<>(Set.of(
                "value",
                "count",
                "mean",
                "min",
                "max",
                "median",
                "oneMinuteRate",
                "fiveMinuteRate",
                "95%",
                "99.9%")
        );
    }
}
