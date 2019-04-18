package com.codingchili.core.testing;

import com.codingchili.core.logging.*;

/**
 * Logger mock to capture console output for testing purposes.
 */
public class LoggerMock extends ConsoleLogger {
    private MockLogListener listener;

    public LoggerMock(MockLogListener listener) {
        super(listener.getClass());
        this.listener = listener;
    }

    @Override
    public Logger log(String line) {
        listener.onLogged(line);
        return this;
    }

    @Override
    public Logger log(String line, LogLevel level) {
        listener.onLogged(line);
        return this;
    }
}