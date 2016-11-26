package com.codingchili.core.testing;

import com.codingchili.core.logging.*;

/**
 * @author Robin Duda
 */
public class LoggerMock extends ConsoleLogger {
    private MockLogListener listener;

    public LoggerMock(MockLogListener listener) {
        this.listener = listener;
    }

    @Override
    public Logger log(String line) {
        listener.onLogged(line);
        return this;
    }

    @Override
    public Logger log(String line, Level level) {
        listener.onLogged(line);
        return this;
    }
}