package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Routing.Configuration.ListenerSettings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.codingchili.core.Configuration.Strings.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * @author Robin Duda
 */
public class RestRouteRequest implements Request {
    private final RoutingContext context;
    private final HttpServerRequest request;
    private final ListenerSettings settings;
    private JsonObject data;

    public RestRouteRequest(RoutingContext context, HttpServerRequest request, ListenerSettings settings) {
        this.context = context;
        this.request = request;
        this.settings = settings;
    }

    @Override
    public void error() {
        send(INTERNAL_SERVER_ERROR);
    }

    @Override
    public void unauthorized() {
        send(UNAUTHORIZED);
    }

    @Override
    public void write(Object object) {
        if (object instanceof Buffer) {
            send((Buffer) object);
        } else {
            send(object);
        }
    }

    @Override
    public void accept() {
        send(OK);
    }

    @Override
    public void missing() {
        send(NOT_FOUND);
    }

    @Override
    public void conflict() {
        send(CONFLICT);
    }

    @Override
    public void bad() {
        send(BAD_REQUEST);
    }

    @Override
    public String action() {
        String action = request.path();

        try {
            JsonObject data = context.getBodyAsJson();

            if (data != null && data.containsKey(ID_ACTION)) {
                action = data.getString(ID_ACTION);
            }
        } catch (DecodeException ignored) {
        }

        return action;
    }

    @Override
    public String target() {
        String target = NODE_WEBSERVER;

        try {
            if (!context.getBodyAsString().isEmpty()) {
                JsonObject data = context.getBodyAsJson();

                if (data != null && data.containsKey(ID_TARGET)) {
                    target = data.getString(ID_TARGET);
                }
            }
        } catch (DecodeException ignored) {
            target = "";
        }

        return target;
    }

    @Override
    public Token token() {
        throw new RuntimeException("The routing node does not handle authentication.");
    }

    @Override
    public JsonObject data() {
        if (data == null) {
            try {
                data = context.getBodyAsJson().put(ID_ACTION, action());
            } catch (DecodeException e) {
                data = new JsonObject().put(ID_ACTION, request.path());
            }
        }

        return data;
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }

    private void send(HttpResponseStatus status) {
        request.response().setStatusCode(status.code()).end();
    }

    private void send(Buffer buffer) {
        request.response().end(buffer);
    }

    private void send(Object object) {
        request.response().end(Buffer.buffer(object.toString()));
    }
}
