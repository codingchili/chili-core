package com.codingchili.core.Logging;

/**
 * @author Robin Duda
 */
public interface StringLogger {
    StringLogger log(String line);

    StringLogger log(String line, Level level);
}
