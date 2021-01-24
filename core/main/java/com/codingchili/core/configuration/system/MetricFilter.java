package com.codingchili.core.configuration.system;

import com.codingchili.core.files.Configurations;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration used to filter metrics.
 */
public class MetricFilter {
    private Set<String> include = new HashSet<>();
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
    public MetricFilter include(String key) {
        include.add(key);
        return this;
    }

    /**
     * @param key the key to exclude from entries matching the filter.
     * @return fluent.
     */
    public MetricFilter exclude(String key) {
        exclude.add(key);
        return this;
    }

    /**
     * Applies the filtering logic to a metrics object.
     *
     * @param capture the metrics object to filter out keys from.
     * @param root    the root object to attach filtered metrics to.
     */
    public void apply(JsonObject capture, JsonObject root) {
        var settings = Configurations.system().getMetrics();

        // iterate over captured metrics objects.
        capture.forEach(entry -> {
            // iterate over individual metric objects.
            var iterator = capture.getJsonObject(entry.getKey()).iterator();

            // filter fields in each metric.
            while (iterator.hasNext()) {
                var next = iterator.next();
                var key = next.getKey();

                var included = (settings.getIncludes().contains(key) && !exclude.contains(key))
                        || include.contains(key);

                if (!included) {
                    iterator.remove();
                }
            }
            // rename the key of the metrics object if alias is set.
            if (alias != null) {
                root.put(entry.getKey().replace(path, alias), entry.getValue());
            } else {
                root.put(entry.getKey(), entry.getValue());
            }
        });
    }
}
