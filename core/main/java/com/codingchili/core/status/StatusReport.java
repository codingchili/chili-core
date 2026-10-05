package com.codingchili.core.status;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.management.*;
import java.time.Instant;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.Environment;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.context.CoreContext;

/**
 * Creates the status report of an application from what can be observed about the JVM, vertx and the context.
 * <p>
 * The report contains no configuration values that are secret, and none that can be used to reach anything:
 * counts, sizes and timeouts only.
 */
public class StatusReport {
    public static final String STATE_RUNNING = "RUNNING";
    public static final String STATE_SHUTTING_DOWN = "SHUTTING_DOWN";
    private static final double MB = 1024.0 * 1024.0;

    private StatusReport() {
    }

    /**
     * @param core      the context to report on.
     * @param readiness the result of the readiness checks, see {@link StatusService}.
     * @return a future completed with the full report.
     */
    public static Future<JsonObject> create(CoreContext core, JsonObject readiness) {
        return metrics(core).map(metrics -> new JsonObject()
                .put("state", state(core))
                .put("ready", readiness.getBoolean("ready"))
                .put("timestamp", Instant.now().toString())
                .put("application", application())
                .put("readiness", readiness)
                .put("jvm", jvm())
                .put("vertx", vertx(core))
                .put("settings", settings(core))
                .put("metrics", metrics));
    }

    /**
     * @param core the context to get the state of.
     * @return {@link #STATE_SHUTTING_DOWN} or {@link #STATE_RUNNING}.
     */
    public static String state(CoreContext core) {
        return core.isShuttingDown() ? STATE_SHUTTING_DOWN : STATE_RUNNING;
    }

    private static Future<JsonObject> metrics(CoreContext core) {
        if (core.system().getMetrics().isEnabled() && core.metrics() != null) {
            return core.metrics().snapshot().otherwise(e -> new JsonObject());
        } else {
            return Future.succeededFuture(new JsonObject());
        }
    }

    private static JsonObject application() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

        return new JsonObject()
                .put("version", String.valueOf(CoreStrings.VERSION))
                .put("hostname", Environment.hostname().orElse("n/a"))
                .put("pid", ProcessHandle.current().pid())
                .put("startTime", Instant.ofEpochMilli(runtime.getStartTime()).toString())
                .put("uptimeMs", runtime.getUptime());
    }

    private static JsonObject jvm() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

        JsonArray collectors = new JsonArray();
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            collectors.add(new JsonObject()
                    .put("name", gc.getName())
                    .put("collections", gc.getCollectionCount())
                    .put("timeMs", gc.getCollectionTime()));
        }

        JsonObject osInfo = new JsonObject()
                .put("name", os.getName())
                .put("arch", os.getArch())
                .put("processors", os.getAvailableProcessors())
                .put("loadAverage", os.getSystemLoadAverage());

        if (os instanceof com.sun.management.OperatingSystemMXBean extended) {
            osInfo.put("processCpuLoad", extended.getProcessCpuLoad());
            osInfo.put("systemCpuLoad", extended.getCpuLoad());
        }

        return new JsonObject()
                .put("java", Runtime.version().toString())
                .put("vm", runtime.getVmName())
                .put("heap", usage(memory.getHeapMemoryUsage()))
                .put("nonHeap", usage(memory.getNonHeapMemoryUsage()))
                .put("threads", new JsonObject()
                        .put("live", threads.getThreadCount())
                        .put("daemon", threads.getDaemonThreadCount())
                        .put("peak", threads.getPeakThreadCount()))
                .put("gc", collectors)
                .put("classesLoaded", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount())
                .put("os", osInfo);
    }

    private static JsonObject usage(MemoryUsage usage) {
        return new JsonObject()
                .put("usedMb", round(usage.getUsed() / MB))
                .put("committedMb", round(usage.getCommitted() / MB))
                // the maximum is undefined (-1) for some memory pools.
                .put("maxMb", (usage.getMax() < 0) ? null : round(usage.getMax() / MB));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static JsonObject vertx(CoreContext core) {
        var options = core.system().getOptions();

        return new JsonObject()
                .put("clustered", core.vertx().isClustered())
                .put("nativeTransport", core.vertx().isNativeTransportEnabled())
                .put("deployments", core.vertx().deploymentIDs().size())
                .put("blockingTasks", core.blockingTasks())
                .put("eventLoopPoolSize", options.getEventLoopPoolSize())
                .put("workerPoolSize", options.getWorkerPoolSize());
    }

    private static JsonObject settings(CoreContext core) {
        SystemSettings system = core.system();

        return new JsonObject()
                .put("listeners", system.getListeners())
                .put("handlers", system.getHandlers())
                .put("services", system.getServices())
                .put("clusterTimeoutMs", system.getClusterTimeout())
                .put("shutdownHookTimeoutMs", system.getShutdownHookTimeout())
                .put("shutdownDelayMs", system.getShutdownDelay())
                .put("configurationReload", system.isConfigurationReload())
                .put("prettyEncoding", system.isPrettyEncoding())
                .put("metricsEnabled", system.getMetrics().isEnabled());
    }
}
