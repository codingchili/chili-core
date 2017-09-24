package com.codingchili.logging.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.security.Token;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.json.JsonObject;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 * <p>
 * Log handler for messages incoming from clients.
 */
public class ClientLogHandler extends AbstractLogHandler {

    public ClientLogHandler(LogContext context) {
        super(context, NODE_CLIENT_LOGGING);
    }

    @Override
    protected void log(Request request) {
        JsonObject logdata = request.data();

        if (verifyToken(logdata)) {
            logdata.remove(ID_TOKEN);
            logdata.remove(PROTOCOL_ROUTE);
            console.log(logdata);
            store.log(logdata);
        } else {
            request.error(new AuthorizationRequiredException());
        }
    }

    private boolean verifyToken(JsonObject logdata) {
        if (logdata.containsKey(ID_TOKEN)) {
            return context.verifyToken(Serializer.unpack(logdata.getJsonObject(ID_TOKEN), Token.class));
        } else {
            return false;
        }
    }
}
