package com.codingchili.core.configuration.system;

import com.codingchili.core.metrics.MetricSettings;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.VertxOptions;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.Environment;

import static com.codingchili.core.configuration.CoreStrings.PATH_SYSTEM;

/**
 * Contains settings for the core system.
 */
public class SystemSettings implements Configurable {
    private MetricSettings metrics = new MetricSettings();
    private VertxOptions options = null;
    private int services = 1;
    private int handlers = 1;
    private int listeners = 1;
    private int deployTimeout = 3000;
    private int shutdownHookTimeout = 5000;
    private int configurationPoll = 1500;
    private int cachedFilePoll = 1500;
    private boolean consoleLogging = true;
    private boolean unsafe = false;
    private int clusterTimeout = 3000;
    private long blockedThreadChecker = VertxOptions.DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL;
    private long maxEventLoopExecuteTime = VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME / (1000 * 1000);
    private int workerPoolSize = Math.min(
            Runtime.getRuntime().availableProcessors() * 8, // up to 8 core.
            32 + Runtime.getRuntime().availableProcessors() * 4); // over 8 cores.

    {
        System.setProperty("kryo.unsafe", String.valueOf(unsafe));
    }

    @Override
    public String getPath() {
        return PATH_SYSTEM;
    }

    /**
     * @return metric settings with paths and includes.
     */
    public MetricSettings getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricSettings metrics) {
        this.metrics = metrics;
    }

    /**
     * @return the number of handlers to deploy for each name.
     */
    public int getHandlers() {
        return handlers;
    }

    /**
     * @param handlers get the number of handlers to deploy for each name.
     *                 This is a recommendation based on the available processors.
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
     * @return the number of milliseconds to wait before logging an error if an event
     * loop thread is blocked.
     */
    public long getBlockedThreadChecker() {
        return blockedThreadChecker;
    }

    /**
     * @param blockedThreadChecker sets the number of milliseconds to wait before logging an
     *                             error if an event loop thread is blocked.
     */
    public void setBlockedThreadChecker(long blockedThreadChecker) {
        this.blockedThreadChecker = blockedThreadChecker;
    }

    /**
     * @return the max event loop execute time the blocked thread checker uses in milliseconds.
     */
    public long getMaxEventLoopExecuteTime() {
        return maxEventLoopExecuteTime;
    }

    /**
     * @param maxEventLoopExecuteTime the number of milliseconds the event loop can block without
     *                                having the blocked-thread-checker generate warning.
     */
    public void setMaxEventLoopExecuteTime(long maxEventLoopExecuteTime) {
        this.maxEventLoopExecuteTime = maxEventLoopExecuteTime;
    }

    /**
     * @return a set of VertxOptions used for deployment.
     */
    @JsonIgnore
    public VertxOptions getOptions() {
        // only reuse options if explicitly configured - when vertx is started in clustered
        // mode it will set clustered = true, if creating a new instance without clustering
        // an exception will be thrown. This causes most test cases to fail.
        if (options == null) {
            options = new VertxOptions()
                    .setMetricsOptions(metrics.createVertxOptions())
                    .setWorkerPoolSize(workerPoolSize)
                    .setBlockedThreadCheckInterval(blockedThreadChecker)
                    .setMaxEventLoopExecuteTime(maxEventLoopExecuteTime * 1000 * 1000);

            options.getEventBusOptions().setHost(Environment.address());
        }
        return options;
    }

    @JsonIgnore
    public SystemSettings setOptions(VertxOptions options) {
        this.options = options;
        this.metrics.setEnabled(options.getMetricsOptions().isEnabled());
        return this;
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

    public boolean isUnsafe() {
        return unsafe;
    }

    /**
     * @param unsafe true if access to Unsafe should be attempted.
     *               this will trigger a warning on most recent JVM's.
     */
    public void setUnsafe(Boolean unsafe) {
        this.unsafe = unsafe;
        System.setProperty("kryo.unsafe", unsafe.toString());
    }
}
