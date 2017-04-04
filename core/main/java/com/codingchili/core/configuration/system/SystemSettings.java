package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.CoreStrings;

/**
 * @author Robin Duda
 *         <p>
 *         Contains settings for the core system.
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

    /**
     * @return the interval in MS which metrics are gathered.
     */
    public int getMetricRate() {
        return metricRate;
    }

    /**
     * @param metricRate sets the interval in MS which metrics are collected.
     */
    public void setMetricRate(int metricRate) {
        this.metricRate = metricRate;
    }

    /**
     * @return the number of handlers to deploy for each service.
     */
    public int getHandlers() {
        return handlers;
    }

    /**
     * @param handlers get the number of handlers to deploy for each service.
     *                 This is a recommendataion based on the available processors.
     */
    public void setHandlers(int handlers) {
        this.handlers = handlers;
    }

    /**
     * @return the time in MS for a deploy to time out and fail.
     */
    public int getDeployTimeout() {
        return deployTimeout;
    }

    /**
     * @param deployTimeout the time in MS for a deploy to time out and fail.
     */
    public void setDeployTimeout(int deployTimeout) {
        this.deployTimeout = deployTimeout;
    }

    /**
     * @return the timeout in MS which the log handler has to report to a remote
     * or local log repository, before the service shuts down.
     */
    public int getShutdownLogTimeout() {
        return shutdownLogTimeout;
    }

    /**
     * @param shutdownLogTimeout sets the time in MS which the log handler has to report
     *                           to a remote or local log repository before the service is shut down.
     */
    public void setShutdownLogTimeout(int shutdownLogTimeout) {
        this.shutdownLogTimeout = shutdownLogTimeout;
    }

    /**
     * @return get the total timeout in MS for the shutdown-hook to complete.
     */
    public int getShutdownHookTimeout() {
        return shutdownHookTimeout;
    }

    /**
     * @param shutdownHookTimeout set the shutdown hook timeout in MS.
     */
    public void setShutdownHookTimeout(int shutdownHookTimeout) {
        this.shutdownHookTimeout = shutdownHookTimeout;
    }

    /**
     * @return returns true if metrics are configured.
     */
    public boolean isMetrics() {
        return metrics;
    }

    /**
     * @param metrics if true enables the gathering of metrics, metrics are then logged
     *                to the configured logger.
     */
    public void setMetrics(boolean metrics) {
        this.metrics = metrics;
    }

    /**
     * @return a set of VertxOptions used for deployment.
     */
    @JsonIgnore
    public VertxOptions getOptions() {
        return new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(metrics));
    }

    /**
     * @return true if allowed to log to console.
     */
    public boolean isConsoleLogging() {
        return consoleLogging;
    }

    /**
     * @param consoleLogging indicates if logging to console is allowed.
     */
    public void setConsoleLogging(boolean consoleLogging) {
        this.consoleLogging = consoleLogging;
    }

    /**
     * @return get the interval in MS in which configurations are checked for changes.
     */
    public int getConfigurationPoll() {
        return configurationPoll;
    }

    /**
     * @param configurationPoll set the interval time in MS which configuration files are checked for changes.
     */
    public void setConfigurationPoll(int configurationPoll) {
        this.configurationPoll = configurationPoll;
    }

    /**
     * @return get the interval in MS for which files that are cached are refreshed.
     */
    public int getCachedFilePoll() {
        return cachedFilePoll;
    }

    /**
     * @param cachedFilePoll set the time in ms for which files that are cached are refreshed.
     */
    public void setCachedFilePoll(int cachedFilePoll) {
        this.cachedFilePoll = cachedFilePoll;
    }

    /**
     * @return the number of workers in the worker pool.
     */
    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    /**
     * @param workerPoolSize sets the number of workers in worker pools.
     *                       must be called before the launcher is invoked.
     */
    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }
}
