package com.codingchili.logging;

import io.vertx.core.Vertx;

import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.configuration.LogServerSettings;

/**
 * @author Robin Duda
 */
class ContextMock extends LogContext {
    private LogServerSettings settings;

    ContextMock(LogServerSettings settings, Vertx vertx) {
        super(vertx);
        settings.getElastic().setEnabled(false);
        this.settings = settings;
    }

    @Override
    public LogServerSettings service() {
        return settings;
    }

}
