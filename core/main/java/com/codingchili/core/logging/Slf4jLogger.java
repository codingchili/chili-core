package com.codingchili.core.logging;

import org.slf4j.Marker;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Forwards logging from dependencies that use SLF4J to a {@link ConsoleLogger}.
 * <p>
 * Debug and trace levels are disabled.
 */
public class Slf4jLogger extends LegacyAbstractLogger {
    private final ConsoleLogger logger = new ConsoleLogger(Slf4jLogger.class);

    /**
     * @param name the name of the SLF4J logger, usually the class name of the caller.
     */
    public Slf4jLogger(String name) {
        this.name = name;
        String source = name.substring(name.lastIndexOf('.') + 1);
        logger.setMetadataValue(LOG_SOURCE, () -> source);
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(org.slf4j.event.Level level, Marker marker, String pattern,
                                               Object[] arguments, Throwable throwable) {
        LogMessage message = logger.event(LOG_SLF4J, toLevel(level));

        if (throwable != null) {
            message.put(LOG_STACKTRACE, throwableToString(throwable));
        }
        message.send(MessageFormatter.basicArrayFormat(pattern, arguments));
    }

    private static Level toLevel(org.slf4j.event.Level level) {
        return switch (level) {
            case ERROR -> Level.ERROR;
            case WARN -> Level.WARNING;
            default -> Level.INFO;
        };
    }
}
