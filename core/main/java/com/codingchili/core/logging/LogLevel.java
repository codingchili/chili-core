package com.codingchili.core.logging;

import org.fusesource.jansi.Ansi;

import java.util.HashMap;
import java.util.Map;

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
     * @param ansi the ansi instance to apply styling to, see
     *             {@link Ansi#fg(Ansi.Color)} or {@link Ansi#fgBright(Ansi.Color)}.
     * @return ansi instance with color updated.
     */
    Ansi apply(Ansi ansi);
}
