package com.codingchili.services.logging.controller;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Request;

import com.codingchili.services.logging.configuration.LogContext;

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
        logdata.remove(PROTOCOL_ROUTE);


        console.log(logdata);
        elastic.log(logdata);
    }
}
