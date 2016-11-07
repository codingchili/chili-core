package com.codingchili.services.Logging;

import io.vertx.core.Vertx;

import com.codingchili.services.Logging.Configuration.LogContext;
import com.codingchili.services.Logging.Configuration.LogServerSettings;

/**
 * @author Robin Duda
 */
class ContextMock extends LogContext {
    private LogServerSettings settings;

    ContextMock(LogServerSettings settings) {
        super(Vertx.vertx());
        settings.getElastic().setEnabled(false);
        this.settings = settings;
    }

    @Override
    public LogServerSettings service() {
        return settings;
    }

}
