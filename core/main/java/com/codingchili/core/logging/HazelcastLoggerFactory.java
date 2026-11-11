package com.codingchili.core.logging;

import com.hazelcast.config.Config;
import com.hazelcast.logging.AbstractLogger;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.LogEvent;
import com.hazelcast.logging.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import static com.codingchili.core.configuration.CoreStrings.LOG_SOURCE;
import static com.codingchili.core.configuration.CoreStrings.LOG_STACKTRACE;
import static com.codingchili.core.configuration.CoreStrings.throwableToString;

/**
 * Hazelcast logging through chili-core's console logger: every record is an event {@code hazelcast} with the class
 * as the source and the message on one line, without Hazelcast's prefix ({@code [host]:port [cluster] [version]}).
 * <p>
 * Activated by {@link #install(Config)} when chili-core starts a clustered Vert.x, through the system property
 * {@value #PROPERTY_LOGGING_CLASS}, unless {@value #PROPERTY_LOGGING_CLASS} or {@value #PROPERTY_LOGGING_TYPE} is
 * set (as a system property, or the type in the Hazelcast config).
 * <p>
 * Levels: a record is logged when the JUL logger of its category logs the level, INFO by default; for example
 * {@code com.hazelcast.level=FINE} in a JUL configuration shows the FINE records. A few chatty startup categories
 * (the logo, hints about disabled features, connections, partitions) are treated as FINE. SEVERE is logged as ERROR, below INFO as DEBUG.
 */
public class HazelcastLoggerFactory implements LoggerFactory {
    /**
     * Hazelcast's system property that names a {@link LoggerFactory} class.
     */
    public static final String PROPERTY_LOGGING_CLASS = "hazelcast.logging.class";
    /**
     * Hazelcast's system property (and config property) that picks a built-in logging: jdk, slf4j, log4j2, none.
     */
    public static final String PROPERTY_LOGGING_TYPE = "hazelcast.logging.type";
    // [172.18.0.2]:5701 [cluster] [5.6.0] message; before the address is known: [cluster] [5.6.0] message.
    private static final Pattern PREFIX = Pattern.compile("^(\\[[^\\]]*\\]:\\d+ )?\\[[^\\]]*\\] \\[\\d[^\\]]*\\] ?");
    private static final Set<String> QUIET = Set.of(
            "com.hazelcast.system.logo",
            "com.hazelcast.system.security",
            "com.hazelcast.internal.diagnostics",
            // every connection opened, closed or refused; failures that matter are warnings.
            "com.hazelcast.internal.server.tcp",
            "com.hazelcast.internal.partition",
            "com.hazelcast.transaction"
    );
    // INFO messages about features that are off by design, treated as FINE.
    private static final Set<String> QUIET_MESSAGES = Set.of(
            "Integrity Checker is disabled",
            "The Jet engine is disabled",
            "Diagnostics service is disabled"
    );
    private final Map<String, ILogger> loggers = new ConcurrentHashMap<>();
    private final Logger logger;

    /**
     * Logs to a console logger, as Hazelcast creates it from {@value #PROPERTY_LOGGING_CLASS}.
     */
    public HazelcastLoggerFactory() {
        this(new ConsoleLogger(HazelcastLoggerFactory.class));
    }

    /**
     * @param logger the logger that receives the events.
     */
    public HazelcastLoggerFactory(Logger logger) {
        this.logger = logger;
    }

    /**
     * Makes Hazelcast log through this factory unless a logging is configured: sets the system property
     * {@value #PROPERTY_LOGGING_CLASS} when neither it nor {@value #PROPERTY_LOGGING_TYPE} is a system property
     * and the Hazelcast config has no {@value #PROPERTY_LOGGING_TYPE}. Call it before Hazelcast starts.
     *
     * @param config the Hazelcast config that is about to start, null if not loaded yet.
     * @return true if this factory was installed.
     */
    public static boolean install(Config config) {
        if (isSet(System.getProperty(PROPERTY_LOGGING_CLASS)) || isSet(System.getProperty(PROPERTY_LOGGING_TYPE))) {
            return false;
        }
        if (config != null && isSet(config.getProperty(PROPERTY_LOGGING_TYPE))) {
            return false;
        }
        System.setProperty(PROPERTY_LOGGING_CLASS, HazelcastLoggerFactory.class.getName());
        return true;
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }

    @Override
    public ILogger getLogger(String name) {
        return loggers.computeIfAbsent(name, HazelcastLogger::new);
    }

    /**
     * @param message a message of Hazelcast.
     * @return the message without Hazelcast's member prefix, on one line.
     */
    static String format(String message) {
        if (message == null) {
            return "";
        }
        return DependencyLogs.compact(PREFIX.matcher(message).replaceFirst(""));
    }

    private class HazelcastLogger extends AbstractLogger {
        private final java.util.logging.Logger level;
        private final String source;
        private final boolean quiet;

        HazelcastLogger(String name) {
            this.level = java.util.logging.Logger.getLogger(name);
            this.source = DependencyLogs.source(name);
            this.quiet = QUIET.stream().anyMatch(name::startsWith);
        }

        @Override
        public void log(java.util.logging.Level level, String message) {
            log(level, message, null);
        }

        @Override
        public void log(java.util.logging.Level level, String message, Throwable thrown) {
            String text = format((message == null && thrown != null) ? thrown.getMessage() : message);

            if (isLoggable(level) && !(isQuiet(text, level) && !this.level.isLoggable(java.util.logging.Level.FINE))) {
                LogMessage event = logger.event(DependencyLogs.HAZELCAST, DependencyLogs.level(level))
                        .put(LOG_SOURCE, source);

                if (thrown != null) {
                    event.put(LOG_STACKTRACE, throwableToString(thrown));
                }
                event.send(text);
            }
        }

        private boolean isQuiet(String text, java.util.logging.Level level) {
            return level.intValue() < java.util.logging.Level.WARNING.intValue()
                    && QUIET_MESSAGES.stream().anyMatch(text::startsWith);
        }

        @Override
        public void log(LogEvent event) {
            LogRecord record = event.getLogRecord();
            log(record.getLevel(), record.getMessage(), record.getThrown());
        }

        @Override
        public java.util.logging.Level getLevel() {
            java.util.logging.Logger current = level;

            while (current != null && current.getLevel() == null) {
                current = current.getParent();
            }
            return (current == null) ? java.util.logging.Level.INFO : current.getLevel();
        }

        @Override
        public boolean isLoggable(java.util.logging.Level level) {
            if (quiet && level.intValue() < java.util.logging.Level.WARNING.intValue()) {
                // chatty at INFO: shown only when the category logs FINE.
                return this.level.isLoggable(java.util.logging.Level.FINE);
            }
            return this.level.isLoggable(level);
        }
    }
}
