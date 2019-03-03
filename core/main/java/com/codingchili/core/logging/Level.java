package com.codingchili.core.logging;

/**
 * logging level definitions.
 */
public enum Level {
    INFO(Color.CYAN),
    WARNING(Color.YELLOW),
    STARTUP(Color.GREEN),
    ERROR(Color.RED),
    SPECIAL(Color.PURPLE),
    RESERVED(Color.BLUE),
    NONE(Color.WHITE),
    SEVERE(Color.RED);

    public String color;

    Level(String color) {
        this.color = color;
    }

    public class Color {
        public static final String BLACK = "\u001B[30m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
        public static final String PURPLE = "\u001B[35m";
        public static final String CYAN = "\u001B[36m";
        public static final String WHITE = "\u001B[37m";
    }
}
