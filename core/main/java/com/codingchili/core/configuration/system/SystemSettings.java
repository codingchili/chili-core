package com.codingchili.core.configuration.system;

import com.codingchili.core.metrics.MetricSettings;
import com.codingchili.core.protocol.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.SerializationFeature;
import io.vertx.core.VertxOptions;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.Durations;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Contains settings for the core system.
 */
public class SystemSettings implements Configurable {
    private static final int cores = Runtime.getRuntime().availableProcessors();
    private MetricSettings metrics = new MetricSettings();
    private VertxOptions options = null;
    private int services = 1;
    private int handlers = 1;
    private int listeners = 1;
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration deployTimeout = Duration.ofSeconds(3);
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration shutdownHookTimeout = Duration.ofSeconds(5);
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration shutdownDelay = Duration.ZERO;
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration configurationPoll = Duration.ofMillis(1500);
    private boolean configurationReload = true;
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration cachedFilePoll = Duration.ofMillis(1500);
    private boolean consoleLogging = true;
    private boolean prettyEncoding = false;
    private boolean unsafe = false;
    private boolean strict = false;
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration clusterTimeout = Duration.ofSeconds(3);
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration blockedThreadChecker = Duration.ofMillis(VertxOptions.DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL);
    @JsonDeserialize(using = Durations.Read.class)
    @JsonSerialize(using = Durations.Write.class)
    private Duration maxEventLoopExecuteTime = Duration.ofNanos(VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME);
    private int eventLoops = Math.max(1, cores / 2);
    private int workerPoolSize = Math.max(1, cores / 2);

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
     * @return the time for a deploy to time out and fail (config: 3s; a plain number is ms).
     */
    public Duration getDeployTimeout() {
        return deployTimeout;
    }

    /**
     * @param deployTimeout the time for a deploy to time out and fail.
     * @return fluent
     */
    public SystemSettings setDeployTimeout(Duration deployTimeout) {
        this.deployTimeout = deployTimeout;
        return this;
    }

    /**
     * @return the total time for the shutdown-hook to complete.
     */
    public Duration getShutdownHookTimeout() {
        return shutdownHookTimeout;
    }

    /**
     * @param shutdownHookTimeout the total time for the shutdown-hook to complete.
     * @return fluent
     */
    public SystemSettings setShutdownHookTimeout(Duration shutdownHookTimeout) {
        this.shutdownHookTimeout = shutdownHookTimeout;
        return this;
    }

    /**
     * @return the time that services keep running after a shutdown has started.
     */
    public Duration getShutdownDelay() {
        return shutdownDelay;
    }

    /**
     * When a shutdown starts the context reports that it is shutting down: a readiness check fails, which lets
     * a load balancer stop sending traffic. Services are stopped after this delay, so that requests that are
     * still being sent are served instead of failing. The delay counts towards the shutdown hook timeout, which
     * should be set to more than the delay and the time it takes to stop the services. Default 0: no delay.
     *
     * @param shutdownDelay the time to wait between marking the context as shutting down and stopping services.
     * @return fluent
     */
    public SystemSettings setShutdownDelay(Duration shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
        return this;
    }

    /**
     * @return the interval in which the blocked thread checker checks if an event
     * loop thread is blocked.
     */
    public Duration getBlockedThreadChecker() {
        return blockedThreadChecker;
    }

    /**
     * @param blockedThreadChecker sets the interval in which the blocked thread checker checks if an
     *                             event loop thread is blocked.
     * @return fluent
     */
    public SystemSettings setBlockedThreadChecker(Duration blockedThreadChecker) {
        this.blockedThreadChecker = blockedThreadChecker;
        return this;
    }

    /**
     * @return the max event loop execute time the blocked thread checker uses.
     */
    public Duration getMaxEventLoopExecuteTime() {
        return maxEventLoopExecuteTime;
    }

    /**
     * @param maxEventLoopExecuteTime the time the event loop can block without
     *                                having the blocked-thread-checker generate warning.
     * @return fluent
     */
    public SystemSettings setMaxEventLoopExecuteTime(Duration maxEventLoopExecuteTime) {
        this.maxEventLoopExecuteTime = maxEventLoopExecuteTime;
        return this;
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
                    .setEventLoopPoolSize(eventLoops)
                    .setWorkerPoolSize(workerPoolSize)
                    .setBlockedThreadCheckInterval(blockedThreadChecker.toMillis())
                    .setBlockedThreadCheckIntervalUnit(TimeUnit.MILLISECONDS)
                    .setMaxEventLoopExecuteTime(maxEventLoopExecuteTime.toNanos())
                    .setMaxEventLoopExecuteTimeUnit(TimeUnit.NANOSECONDS);
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
     * @return the interval in which configurations are checked for changes.
     */
    public Duration getConfigurationPoll() {
        return configurationPoll;
    }

    /**
     * @param configurationPoll the interval in which configuration files are checked for changes.
     * @return fluent
     */
    public SystemSettings setConfigurationPoll(Duration configurationPoll) {
        this.configurationPoll = configurationPoll;
        return this;
    }

    /**
     * @return true if configuration files are reloaded when they change on disk.
     */
    public boolean isConfigurationReload() {
        return configurationReload;
    }

    /**
     * Hot reloading of configuration is enabled by default. When disabled configuration files are read once,
     * and changes on disk are ignored until the application is restarted: nothing that can write to the
     * configuration directory can change the behavior of a running application. It is recommended to disable
     * hot reload in production.
     * <p>
     * Once disabled, reloading cannot be enabled again at runtime, as changes to this setting on disk are ignored too.
     *
     * @param configurationReload false to disable reloading of configuration files when they change.
     * @return fluent
     */
    public SystemSettings setConfigurationReload(boolean configurationReload) {
        this.configurationReload = configurationReload;
        return this;
    }

    /**
     * @return the interval in which files that are cached are refreshed.
     */
    public Duration getCachedFilePoll() {
        return cachedFilePoll;
    }

    /**
     * @param cachedFilePoll the interval in which files that are cached are refreshed.
     * @return fluent
     */
    public SystemSettings setCachedFilePoll(Duration cachedFilePoll) {
        this.cachedFilePoll = cachedFilePoll;
        return this;
    }

    /**
     * @return the number of event loop threads, defaults to half of the logical processors.
     */
    public int getEventLoops() {
        return eventLoops;
    }

    /**
     * @param eventLoops sets the number of event loop threads, handlers and listeners are spread over these.
     *                   must be called before the launcher is invoked.
     * @return fluent
     */
    public SystemSettings setEventLoops(int eventLoops) {
        this.eventLoops = eventLoops;
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

    /**
     * @return the time until requests over the cluster time out.
     */
    public Duration getClusterTimeout() {
        return clusterTimeout;
    }

    /**
     * @param clusterTimeout the time until requests over the cluster time out.
     * @return fluent
     */
    public SystemSettings setClusterTimeout(Duration clusterTimeout) {
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
    public SystemSettings setUnsafe(Boolean unsafe) {
        this.unsafe = unsafe;
        System.setProperty("kryo.unsafe", unsafe.toString());
        return this;
    }

    public boolean isPrettyEncoding() {
        return prettyEncoding;
    }

    public SystemSettings setPrettyEncoding(boolean prettyEncoding) {
        Serializer.json = Serializer.json.rebuild()
                .configure(SerializationFeature.INDENT_OUTPUT, prettyEncoding)
                .build();
        this.prettyEncoding = prettyEncoding;
        return this;
    }

    /**
     * @return true when a service may only load the configuration files named as the services deployed in its JVM
     * ({@link com.codingchili.core.files.ServiceFiles}): a file with generated secrets loaded outside of them fails
     * the start instead of a warning ({@link com.codingchili.core.files.ConfigurationGuard}). As configured, see
     * {@link #strict()} for the value in effect.
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * @param strict see {@link #isStrict()}, for production (the baked images set {@value CoreStrings#ENV_STRICT}).
     * @return fluent
     */
    public SystemSettings setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    /**
     * @return the strict mode in effect: the system property {@value CoreStrings#PROPERTY_STRICT} or the
     * environment variable {@value CoreStrings#ENV_STRICT} (true/false) when set, else {@link #isStrict()}.
     */
    @JsonIgnore
    public boolean strict() {
        for (String value : new String[]{System.getProperty(PROPERTY_STRICT), System.getenv(ENV_STRICT)}) {
            if (value != null && !value.isBlank()) {
                return Set.of("true", "on", "yes", "1").contains(value.trim().toLowerCase(Locale.ROOT));
            }
        }
        return strict;
    }
}
