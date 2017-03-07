package com.codingchili.logging.controller;

import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Request;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 *
 * Log handler for log messages incoming from services.
 */
public class ServiceLogHandler<T extends LogContext> extends AbstractLogHandler<T> {

    public ServiceLogHandler(T context) {
        super(context, NODE_LOGGING);
    }

    @Override
    protected void log(Request request) {
        JsonObject logdata = request.data();
        String node = logdata.getString(LOG_NODE);

        logdata.remove(PROTOCOL_ROUTE);

        if (!NODE_LOGGING.equals(node) && context().consoleEnabled()) {
            console.log(logdata);
        }
        store.log(logdata);
    }
}
