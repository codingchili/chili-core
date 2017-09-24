package com.codingchili.core.testing;

import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;

/**
 * @author Robin Duda
 * <p>
 * Logger mock to capture console output for testing purposes.
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