package com.codingchili.logging.controller;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Request;

import com.codingchili.logging.configuration.LogContext;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 */
public class ServiceLogHandler<T extends LogContext> extends AbstractLogHandler<T> {

    public ServiceLogHandler(T context) {
        super(context, NODE_LOGGING);
    }

    protected void log(Request request) {
        JsonObject logdata = request.data();
        String node = logdata.getString(LOG_NODE);

        logdata.remove(PROTOCOL_ROUTE);

        if (!NODE_LOGGING.equals(node) && context().consoleEnabled()) {
            console.log(logdata);
        }

        elastic.log(logdata);
    }
}
