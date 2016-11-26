package com.codingchili.core.logging;

/**
 * @author Robin Duda
 *
 * Interface for text-only logger.
 */
interface StringLogger {
    /**
     * @param line the text line to be logged.
     */
    Logger log(String line);

    /**
     * @param line the text line to be logged.
     * @param level the logging level to log with.
     */
    Logger log(String line, Level level);
}
