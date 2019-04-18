package com.codingchili.core.logging;

/**
 * Interface for text-only logger.
 */
interface StringLogger {
    /**
     * @param line the text line to be logged.
     * @return fluent
     */
    Logger log(String line);

    /**
     * @param line  the text line to be logged.
     * @param level the logging level to log with.
     * @return fluent
     */
    Logger log(String line, LogLevel level);
}
