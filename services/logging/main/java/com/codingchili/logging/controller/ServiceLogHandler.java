package com.codingchili.logging.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.json.JsonObject;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 * <p>
 * Log handler for log messages incoming from services.
 */
public class ServiceLogHandler extends AbstractLogHandler {

    public ServiceLogHandler(LogContext context) {
        super(context, NODE_LOGGING);
    }

    @Override
    protected void log(Request request) {
        JsonObject logdata = request.data().getJsonObject(ID_MESSAGE);
        String node = logdata.getString(LOG_NODE);

        if (!NODE_LOGGING.equals(node) && context.consoleEnabled()) {
            console.log(logdata);
        }
        store.log(logdata);
    }
}
