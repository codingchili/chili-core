package com.codingchili.services.Logging.Controller;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.Protocol.Request;

import com.codingchili.services.Logging.Configuration.LogContext;

import static com.codingchili.services.Shared.Strings.*;


/**
 * @author Robin Duda
 */
public class ClientLogHandler<T extends LogContext> extends AbstractLogHandler<T> {

    public ClientLogHandler(T context) {
        super(context, NODE_CLIENT_LOGGING);
    }

    protected void log(Request request) {
        JsonObject logdata = request.data();

        logdata.remove(ID_TOKEN);
        logdata.remove(PROTOCOL_ACTION);


        console.log(logdata);

        elastic.log(logdata);
    }
}