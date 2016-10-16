package com.codingchili.core.Configuration.System;

import com.codingchili.core.Configuration.LoadableConfigurable;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.metrics.MetricsOptions;

import static com.codingchili.core.Configuration.Strings.PATH_VERTX;

/**
 * @author Robin Duda
 */
public class VertxSettings implements LoadableConfigurable {
    public static int METRIC_RATE = 0;
    private static boolean metrics;
    private JsonArray help;
    private int poolSize;
    private int deployTimeout;
    private int shutdownLogTimeout;
    private int shutdownHookTimeout;

    public static VertxOptions Configuration() {
        return new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(metrics));
    }

    public JsonArray getHelp() {
        return help;
    }

    public void setHelp(JsonArray help) {
        this.help = help;
    }

    public boolean isMetrics() {
        return metrics;
    }

    public void setMetrics(boolean metrics) {
        VertxSettings.metrics = metrics;
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
                .setMetricsOptions(new MetricsOptions().setEnabled(metrics))
                .setWorkerPoolSize(poolSize);
    }

    @Override
    public String getPath() {
        return PATH_VERTX;
    }
}
