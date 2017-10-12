package com.codingchili.core.configuration.system;

import com.codingchili.core.configuration.Configurable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;

import static com.codingchili.core.configuration.CoreStrings.PATH_SYSTEM;

/**
 * @author Robin Duda
 * <p>
 * Contains settings for the core system.
 */
public class SystemSettings implements Configurable {
    private int metricRate = 15000;
    private int services = 1;
    private int handlers = Runtime.getRuntime().availableProcessors();
    private int listeners = Runtime.getRuntime().availableProcessors();
    private boolean metrics = false;
    private int deployTimeout = 3000;
    private int shutdownLogTimeout = 3000;
    private int shutdownHookTimeout = 3000;
    private int configurationPoll = 1500;
    private int cachedFilePoll = 1500;
    private boolean consoleLogging = true;
    private int workerPoolSize = 32;
    private int clusterTimeout = 3000;

    @Override
    public String getPath() {
        return PATH_SYSTEM;
    }

    /**
     * @return the interval in MS which metrics are gathered.
     */
    public int getMetricRate() {
        return metricRate;
    }

    /**
     * @param metricRate sets the interval in MS which metrics are collected.
     * @return fluent
     */
    public SystemSettings setMetricRate(int metricRate) {
        this.metricRate = metricRate;
        return this;
    }

    /**
     * @return the number of handlers to deploy for each name.
     */
    public int getHandlers() {
        return handlers;
    }

    /**
     * @param handlers get the number of handlers to deploy for each name.
     *                 This is a recommendataion based on the available processors.
     * @return fluent
     */
    public SystemSettings setHandlers(int handlers) {
        this.handlers = handlers;
        return this;
    }

    /**
     * @return the time in MS for a deploy to time out and fail.
     */
    public int getDeployTimeout() {
        return deployTimeout;
    }

    /**
     * @param deployTimeout the time in MS for a deploy to time out and fail.
     * @return fluent
     */
    public SystemSettings setDeployTimeout(int deployTimeout) {
        this.deployTimeout = deployTimeout;
        return this;
    }

    /**
     * @return the timeout in MS which the log handler has to report to a remote
     * or local log repository, before the name shuts down.
     */
    public int getShutdownLogTimeout() {
        return shutdownLogTimeout;
    }

    /**
     * @param shutdownLogTimeout sets the time in MS which the log handler has to report
     *                           to a remote or local log repository before the name is shut down.
     * @return fluent
     */
    public SystemSettings setShutdownLogTimeout(int shutdownLogTimeout) {
        this.shutdownLogTimeout = shutdownLogTimeout;
        return this;
    }

    /**
     * @return get the total timeout in MS for the shutdown-hook to complete.
     */
    public int getShutdownHookTimeout() {
        return shutdownHookTimeout;
    }

    /**
     * @param shutdownHookTimeout set the shutdown hook timeout in MS.
     * @return fluent
     */
    public SystemSettings setShutdownHookTimeout(int shutdownHookTimeout) {
        this.shutdownHookTimeout = shutdownHookTimeout;
        return this;
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
     * @return fluent
     */
    public SystemSettings setMetrics(boolean metrics) {
        this.metrics = metrics;
        return this;
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
     * @return fluent
     */
    public SystemSettings setConsoleLogging(boolean consoleLogging) {
        this.consoleLogging = consoleLogging;
        return this;
    }

    /**
     * @return get the interval in MS in which configurations are checked for changes.
     */
    public int getConfigurationPoll() {
        return configurationPoll;
    }

    /**
     * @param configurationPoll set the interval time in MS which configuration files are checked for changes.
     * @return fluent
     */
    public SystemSettings setConfigurationPoll(int configurationPoll) {
        this.configurationPoll = configurationPoll;
        return this;
    }

    /**
     * @return get the interval in MS for which files that are cached are refreshed.
     */
    public int getCachedFilePoll() {
        return cachedFilePoll;
    }

    /**
     * @param cachedFilePoll set the time in ms for which files that are cached are refreshed.
     * @return fluent
     */
    public SystemSettings setCachedFilePoll(int cachedFilePoll) {
        this.cachedFilePoll = cachedFilePoll;
        return this;
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
     * @return fluent
     */
    public SystemSettings setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
        return this;
    }

    public int getClusterTimeout() {
        return clusterTimeout;
    }

    /**
     * @param clusterTimeout cluster timeout in seconds
     * @return fluent
     */
    public SystemSettings setClusterTimeout(int clusterTimeout) {
        this.clusterTimeout = clusterTimeout;
        return this;
    }

    public int getListeners() {
        return listeners;
    }

    /**
     * @param listeners number of listeners to deploy by default
     * @return fluent
     */
    public SystemSettings setListeners(int listeners) {
        this.listeners = listeners;
        return this;
    }

    public int getServices() {
        return services;
    }

    /**
     * @param services number of services to deploy by default
     * @return fluent
     */
    public SystemSettings setServices(int services) {
        this.services = services;
        return this;
    }
}
