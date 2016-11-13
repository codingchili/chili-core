package com.codingchili.services.Logging.Controller;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.Protocol.Request;

import com.codingchili.services.Logging.Configuration.LogContext;

import static com.codingchili.services.Shared.Strings.*;


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
