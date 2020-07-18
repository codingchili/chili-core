package com.codingchili.core.logging;

import org.fusesource.jansi.Ansi;

/**
 * logging level definitions.
 */
public enum Level implements LogLevel {
    INFO(Ansi.Color.CYAN),
    WARNING(Ansi.Color.YELLOW),
    ERROR(Ansi.Color.RED),
    SPECIAL(Ansi.Color.MAGENTA),
    RESERVED(Ansi.Color.BLUE),
    NONE(Ansi.Color.WHITE),
    SEVERE(Ansi.Color.RED);

    private Ansi.Color color;

    /**
     * Changes the default color of the log level to the given.
     *
     * @param color see {@link Ansi.Color}
     */
    public void setColor(Ansi.Color color) {
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
    public Ansi apply(Ansi ansi) {
        // default to always use the bright color as windows has major issues with the others.
        // for example, magenta is blue and yellow is white.
        return ansi.fgBright(color);
    }
}
