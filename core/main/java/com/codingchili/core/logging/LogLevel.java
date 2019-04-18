package com.codingchili.core.logging;

import org.fusesource.jansi.Ansi;

import java.util.*;

/**
 * This interface allows for custom defined logging levels.
 */
public interface LogLevel {
    Map<String, LogLevel> registered = new HashMap<>();

    static void register(LogLevel level) {
        registered.put(level.getName(), level);
    }

    /**
     * @return the name of the logging level.
     */
    String getName();

    /**
     * @return the ANSI color to use for this level.
     */
    Ansi.Color getColor();
}
