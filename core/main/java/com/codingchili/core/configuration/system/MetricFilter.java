package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration used to filter metrics.
 */
public class MetricFilter {
    private String path;
    private String alias;
    private Set<String> include = new HashSet<>();

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

    public MetricFilter include(String include) {
        this.include.add(include);
        return this;
    }

    public void setInclude(Set<String> include) {
        this.include = include;
    }

    /**
     * Applies the filtering logic to a metrics object.
     *
     * @param capture the metrics object to filter out keys from.
     * @param root    the root object to attach filtered metrics to.
     */
    public void apply(JsonObject capture, JsonObject root) {
        // iterate over captured metrics objects.
        capture.forEach(entry -> {
            // iterate over individual metric objects.
            var iterator = capture.getJsonObject(entry.getKey()).iterator();

            // filter fields in each metric.
            while (iterator.hasNext()) {
                var next = iterator.next();
                if (!include.contains(next.getKey())) {
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
