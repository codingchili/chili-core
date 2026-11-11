package com.codingchili.core.configuration.system;

import com.codingchili.core.metrics.MetricSettings;
import com.codingchili.core.protocol.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.SerializationFeature;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;

import java.util.function.UnaryOperator;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;

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
    private int deployTimeout = 3000;
    private int shutdownHookTimeout = 5000;
    private int shutdownDelay = 0;
    private int configurationPoll = 1500;
    private boolean configurationReload = true;
    private int cachedFilePoll = 1500;
    private boolean consoleLogging = true;
    private boolean prettyEncoding = false;
    private boolean unsafe = false;
    private int clusterTimeout = 3000;
    private long blockedThreadChecker = VertxOptions.DEFAULT_BLOCKED_THREAD_CHECK_INTERVAL;
    private long maxEventLoopExecuteTime = VertxOptions.DEFAULT_MAX_EVENT_LOOP_EXECUTE_TIME / (1000 * 1000);
    private int eventLoops = Math.max(1, cores / 2);
    private int workerPoolSize = Math.max(1, cores / 2);
    private String eventBusHost = null;
    private String eventBusPublicHost = null;
    private int eventBusPort = 0;

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
     * @return the time in MS that services keep running after a shutdown has started.
     */
    public int getShutdownDelay() {
        return shutdownDelay;
    }

    /**
     * When a shutdown starts the context reports that it is shutting down: a readiness check fails, which lets
     * a load balancer stop sending traffic. Services are stopped after this delay, so that requests that are
     * still being sent are served instead of failing. The delay counts towards the shutdown hook timeout, which
     * should be set to more than the delay and the time it takes to stop the services. Default 0: no delay.
     *
     * @param shutdownDelay the time in MS to wait between marking the context as shutting down and stopping services.
     * @return fluent
     */
    public SystemSettings setShutdownDelay(int shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
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
                    .setEventLoopPoolSize(eventLoops)
                    .setWorkerPoolSize(workerPoolSize)
                    .setBlockedThreadCheckInterval(blockedThreadChecker)
                    .setMaxEventLoopExecuteTime(maxEventLoopExecuteTime * 1000 * 1000);

            applyEventBus(options.getEventBusOptions(), System::getenv);
        }
        return options;
    }

    /**
     * Applies the clustered event bus settings to the given options. Each setting is read from its system
     * property, then its environment variable, then this configuration; a setting that is not set anywhere is
     * left unset, so that Vert.x takes it from the cluster manager: Hazelcast's member address (or its public
     * address) for the hosts, and a random port. The event bus is only reachable from other nodes when clustered.
     *
     * @param eventBus    the event bus options to apply the settings to.
     * @param environment looks up environment variables, {@link System#getenv(String)} outside of tests.
     */
    void applyEventBus(EventBusOptions eventBus, UnaryOperator<String> environment) {
        String host = resolve(PROPERTY_EVENTBUS_HOST, ENV_EVENTBUS_HOST, eventBusHost, environment);
        String publicHost = resolve(PROPERTY_EVENTBUS_PUBLIC_HOST, ENV_EVENTBUS_PUBLIC_HOST, eventBusPublicHost, environment);
        String port = resolve(PROPERTY_EVENTBUS_PORT, ENV_EVENTBUS_PORT,
                (eventBusPort > 0) ? Integer.toString(eventBusPort) : null, environment);

        if (host != null) {
            eventBus.setHost(host);
        }
        if (publicHost != null) {
            eventBus.setClusterPublicHost(publicHost);
        }
        if (port != null) {
            try {
                eventBus.setPort(Integer.parseInt(port));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("event bus port '%s' (%s/%s) is not a number.",
                                port, PROPERTY_EVENTBUS_PORT, ENV_EVENTBUS_PORT), e);
            }
        }
    }

    private static String resolve(String property, String env, String configured, UnaryOperator<String> environment) {
        for (String value : new String[]{System.getProperty(property), environment.apply(env), configured}) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * @return the host that the clustered event bus binds to, null when not set.
     */
    public String getEventBusHost() {
        return eventBusHost;
    }

    /**
     * Sets the host (interface) that the clustered event bus binds to, for example 0.0.0.0 for every interface.
     * Unset: Vert.x uses the cluster manager's (Hazelcast's) member address. When set and no public host is
     * set, this host is also the address that other nodes use, so a wildcard address requires a public host.
     * Overridden by the system property {@value CoreStrings#PROPERTY_EVENTBUS_HOST} and the environment variable
     * {@value CoreStrings#ENV_EVENTBUS_HOST}.
     *
     * @param eventBusHost the bind host of the clustered event bus, null to let the cluster manager decide.
     * @return fluent
     */
    public SystemSettings setEventBusHost(String eventBusHost) {
        this.eventBusHost = eventBusHost;
        return this;
    }

    /**
     * @return the host that the clustered event bus advertises to other nodes, null when not set.
     */
    public String getEventBusPublicHost() {
        return eventBusPublicHost;
    }

    /**
     * Sets the address that other cluster nodes use to reach the event bus of this node, when it is not an address
     * of this machine: NAT, containers on separate hosts. Unset: the bind host if set, otherwise the cluster
     * manager's public address (Hazelcast's public-address or the system property hazelcast.local.publicAddress),
     * otherwise the bind host that Vert.x chose. Overridden by the system property
     * {@value CoreStrings#PROPERTY_EVENTBUS_PUBLIC_HOST} and the environment variable
     * {@value CoreStrings#ENV_EVENTBUS_PUBLIC_HOST}.
     *
     * @param eventBusPublicHost the advertised host of the clustered event bus.
     * @return fluent
     */
    public SystemSettings setEventBusPublicHost(String eventBusPublicHost) {
        this.eventBusPublicHost = eventBusPublicHost;
        return this;
    }

    /**
     * @return the port of the clustered event bus, 0 for a random port.
     */
    public int getEventBusPort() {
        return eventBusPort;
    }

    /**
     * Sets a fixed port for the clustered event bus, so that it can be opened in a firewall or mapped by a
     * container runtime. Other nodes connect to the same port. Default 0: a random port.
     * Overridden by the system property {@value CoreStrings#PROPERTY_EVENTBUS_PORT} and the environment variable
     * {@value CoreStrings#ENV_EVENTBUS_PORT}.
     *
     * @param eventBusPort the port of the clustered event bus, 0 for a random port.
     * @return fluent
     */
    public SystemSettings setEventBusPort(int eventBusPort) {
        this.eventBusPort = eventBusPort;
        return this;
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
}
