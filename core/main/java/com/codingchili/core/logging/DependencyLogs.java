package com.codingchili.core.logging;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

/**
 * Shapes the log records of dependencies (Vert.x, Netty, Hazelcast; through JUL, SLF4J or Hazelcast's logging)
 * into the events of the console: an event named after the library, the class as the source, one line.
 */
final class DependencyLogs {
    static final String HAZELCAST = "hazelcast";
    static final String NETTY = "netty";
    static final String VERTX = "vertx";
    private static final SimpleFormatter FORMATTER = new SimpleFormatter();
    private static final Pattern LINE_BREAK = Pattern.compile(",?[ \\t]*\\R\\s*");
    private static final Pattern OPEN_BREAK = Pattern.compile("([\\[{(])[ \\t]*\\R\\s*");
    private static final Pattern CLOSE_BREAK = Pattern.compile(",?[ \\t]*\\R\\s*([\\]})])");

    private DependencyLogs() {
    }

    /**
     * @param loggerName the name of the logger of the dependency, usually a class name.
     * @param fallback   the event name for loggers of other libraries.
     * @return the name of the library as an event name.
     */
    static String event(String loggerName, String fallback) {
        if (loggerName == null) {
            return fallback;
        } else if (loggerName.startsWith("com.hazelcast")) {
            return HAZELCAST;
        } else if (loggerName.startsWith("io.netty")) {
            return NETTY;
        } else if (loggerName.startsWith("io.vertx")) {
            return VERTX;
        } else {
            return fallback;
        }
    }

    /**
     * @param loggerName the name of the logger, usually a class name.
     * @return the last part of the name: the simple class name.
     */
    static String source(String loggerName) {
        if (loggerName == null) {
            return "";
        }
        return loggerName.substring(loggerName.lastIndexOf('.') + 1);
    }

    /**
     * @param level a JUL level.
     * @return the console level: SEVERE is ERROR, WARNING is WARNING, levels below INFO are DEBUG.
     */
    static Level level(java.util.logging.Level level) {
        int value = (level == null) ? java.util.logging.Level.INFO.intValue() : level.intValue();

        if (value >= java.util.logging.Level.SEVERE.intValue()) {
            return Level.ERROR;
        } else if (value >= java.util.logging.Level.WARNING.intValue()) {
            return Level.WARNING;
        } else if (value >= java.util.logging.Level.INFO.intValue()) {
            return Level.INFO;
        } else {
            return Level.DEBUG;
        }
    }

    /**
     * @param record a JUL record.
     * @return the message of the record with its parameters filled in.
     */
    static String message(LogRecord record) {
        return FORMATTER.formatMessage(record);
    }

    /**
     * Joins the lines of a multi-line message into one: "Members {size:2} [\n\tMember a\n\tMember b\n]" becomes
     * "Members {size:2} [Member a, Member b]".
     *
     * @param message the message to compact.
     * @return the message on one line.
     */
    static String compact(String message) {
        if (message == null || message.indexOf('\n') < 0 && message.indexOf('\r') < 0) {
            return message;
        }
        String text = message.strip();
        text = OPEN_BREAK.matcher(text).replaceAll("$1");
        text = CLOSE_BREAK.matcher(text).replaceAll("$1");
        return LINE_BREAK.matcher(text).replaceAll(", ");
    }
}
