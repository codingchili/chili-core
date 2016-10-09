package com.codingchili.core.Configuration;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.metrics.MetricsOptions;

/**
 * @author Robin Duda
 */
public class VertxSettings {
    public static int METRIC_RATE = 0;
    public static boolean METRICS_ENABLED;
    private JsonArray help;
    private int poolSize;
    private int deployTimeout;
    private int shutdownLogTimeout;
    private int shutdownHookTimeout;

    public static VertxOptions Configuration() {
        return new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(METRICS_ENABLED));
    }

    public JsonArray getHelp() {
        return help;
    }

    public void setHelp(JsonArray help) {
        this.help = help;
    }

    public boolean isMetrics() {
        return METRICS_ENABLED;
    }

    public void setMetrics(boolean metrics) {
        VertxSettings.METRICS_ENABLED = metrics;
    }

    public int getRate() {
        return VertxSettings.METRIC_RATE;
    }

    public void setRate(int rate) {
        VertxSettings.METRIC_RATE = rate;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getDeployTimeout() {
        return deployTimeout;
    }

    public void setDeployTimeout(int deployTimeout) {
        this.deployTimeout = deployTimeout;
    }

    public int getShutdownLogTimeout() {
        return shutdownLogTimeout;
    }

    public void setShutdownLogTimeout(int shutdownLogTimeout) {
        this.shutdownLogTimeout = shutdownLogTimeout;
    }

    public int getShutdownHookTimeout() {
        return shutdownHookTimeout;
    }

    public void setShutdownHookTimeout(int shutdownHookTimeout) {
        this.shutdownHookTimeout = shutdownHookTimeout;
    }

    public VertxOptions getOptions() {
        return new VertxOptions()
                .setMetricsOptions(new MetricsOptions().setEnabled(METRICS_ENABLED))
                .setWorkerPoolSize(poolSize);
    }
}
