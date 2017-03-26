package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.CoreStrings;

/**
 * @author Robin Duda
 *
 * Contains settings for the core system.
 */
public class SystemSettings extends BaseConfigurable {
    private int metricRate = 15000;
    private int handlers = Runtime.getRuntime().availableProcessors();
    private boolean metrics = false;
    private int deployTimeout = 3000;
    private int shutdownLogTimeout = 3000;
    private int shutdownHookTimeout = 3000;
    private int configurationPoll = 1500;
    private int cachedFilePoll = 2000;
    private boolean consoleLogging = true;
    private int workerPoolSize = 32;

    public SystemSettings() {
        path = CoreStrings.PATH_SYSTEM;
    }

    public int getMetricRate() {
        return metricRate;
    }

    public void setMetricRate(int metricRate) {
        this.metricRate = metricRate;
    }

    public int getHandlers() {
        return handlers;
    }

    public void setHandlers(int handlers) {
        this.handlers = handlers;
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

    public boolean isMetrics() {
        return metrics;
    }

    public void setMetrics(boolean metrics) {
        this.metrics = metrics;
    }

    @JsonIgnore
    public VertxOptions getOptions() {
        return new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(metrics));
    }

    public boolean isConsoleLogging() {
        return consoleLogging;
    }

    public void setConsoleLogging(boolean consoleLogging) {
        this.consoleLogging = consoleLogging;
    }

    public int getConfigurationPoll() {
        return configurationPoll;
    }

    public void setConfigurationPoll(int configurationPoll) {
        this.configurationPoll = configurationPoll;
    }

    public int getCachedFilePoll() {
        return cachedFilePoll;
    }

    public void setCachedFilePoll(int cachedFilePoll) {
        this.cachedFilePoll = cachedFilePoll;
    }

    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }
}
