package com.codingchili.core.configuration.system;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MetricsFilterTest {
    private static final String ALIAS = "alias";
    private static final String HANDLERS = "vertx.eventbus.handlers";

    @Test
    public void filterUnwantedProperties() {
        var root = new JsonObject();
        var capture = new JsonObject()
                .put(HANDLERS, new JsonObject()
                        .put("type", "metric")
                        .put("count", 12_000)
                        .put("ignored", true)
                );

        filter().include("count")
                .apply(capture, root);

        var metric = root.getJsonObject(HANDLERS);
        Assert.assertTrue(metric.containsKey("count"));
        Assert.assertFalse(metric.containsKey("ignored"));
    }

    @Test
    public void mapMultiplePathsWithAlias() {
        var root = new JsonObject();
        var capture = new JsonObject()
                .put("vertx.net.handlers", new JsonObject())
                .put("vertx.net.socket", new JsonObject());

        filter()
                .setPath("vertx.net")
                .setAlias(ALIAS).apply(capture, root);

        // verify only maps substring of path.
        Assert.assertFalse(root.containsKey(ALIAS));
        Assert.assertTrue(root.containsKey(ALIAS + ".handlers"));
        Assert.assertTrue(root.containsKey(ALIAS + ".socket"));
    }

    private MetricFilter filter() {
        return new MetricFilter();
    }
}
