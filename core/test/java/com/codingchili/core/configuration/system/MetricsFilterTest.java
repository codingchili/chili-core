package com.codingchili.core.configuration.system;

import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.metrics.MetricFilter;

@RunWith(VertxUnitRunner.class)
public class MetricsFilterTest {
    private static final String ALIAS = "alias";
    private static final String HANDLERS = "vertx.eventbus.handlers";
    private Metric timer = new Timer();

    @Test
    public void filterUnwantedProperties() {
        var root = filter()
                .include("count")
                .exclude("fifteenMinuteRate")
                .apply(timer, "vertx.eventbus.handlers");

        var metric = root.getJsonObject(HANDLERS);
        Assert.assertTrue(metric.containsKey("count"));
        Assert.assertFalse(metric.containsKey("fifteenMinuteRate"));
    }

    @Test
    public void mapMultiplePathsWithAlias() {
        var root = filter().setPath("vertx.eventbus")
                .setAlias(ALIAS).apply(timer, "vertx.eventbus.handlers");

        // verify only maps substring of path.
        Assert.assertFalse(root.containsKey(ALIAS));
        Assert.assertTrue(root.containsKey(ALIAS + ".handlers"));
    }

    private MetricFilter filter() {
        return new MetricFilter();
    }
}
