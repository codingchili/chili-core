package com.codingchili.core.logging;

import org.fusesource.jansi.Ansi;

/**
 * logging level definitions.
 */
public enum Level implements LogLevel {
    INFO(Ansi.Color.CYAN),
    WARNING(Ansi.Color.YELLOW),
    STARTUP(Ansi.Color.GREEN),
    ERROR(Ansi.Color.RED),
    SPECIAL(Ansi.Color.MAGENTA),
    RESERVED(Ansi.Color.BLUE),
    NONE(Ansi.Color.WHITE),
    SEVERE(Ansi.Color.RED);

    public Ansi.Color color;

    /**
     * Changes the default color of the log level to the given.
     *
     * @param color defined as an ANSI escape code.
     */
    void setColor(Ansi.Color color) {
        this.color = color;
    }

    Level(Ansi.Color color) {
        this.color = color;
        LogLevel.register(this);
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Ansi.Color getColor() {
        return color;
    }
}
