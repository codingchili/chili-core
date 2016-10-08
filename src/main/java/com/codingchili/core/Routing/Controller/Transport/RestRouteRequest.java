package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Routing.Model.ListenerSettings;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static com.codingchili.core.Configuration.Strings.ID_ACTION;
import static com.codingchili.core.Configuration.Strings.ID_TARGET;
import static com.codingchili.core.Configuration.Strings.NODE_WEBSERVER;

/**
 * @author Robin Duda
 */
public class RestRouteRequest implements Request {
    private RoutingContext context;
    private HttpServerRequest request;
    private ListenerSettings settings;

    public RestRouteRequest(RoutingContext context, HttpServerRequest request, ListenerSettings settings) {
        this.context = context;
        this.request = request;
        this.settings = settings;
    }

    @Override
    public void error() {
        send(request, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public void unauthorized() {
        send(request, HttpResponseStatus.UNAUTHORIZED);
    }

    @Override
    public void write(Object object) {
        if (object instanceof Buffer) {
            send(request, (Buffer) object);
        } else {
            send(request, object);
        }
    }

    @Override
    public void accept() {
        send(request, HttpResponseStatus.OK);
    }

    @Override
    public void missing() {
        send(request, HttpResponseStatus.NOT_FOUND);
    }

    @Override
    public void conflict() {
        send(request, HttpResponseStatus.CONFLICT);
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
            JsonObject data = context.getBodyAsJson();

            if (data != null && data.containsKey(ID_TARGET)) {
                target = data.getString(ID_TARGET);
            }
        } catch (DecodeException ignored) {
        }

        return target;
    }

    @Override
    public Token token() {
        throw new RuntimeException("The routing node does not handle authentication.");
    }

    @Override
    public JsonObject data() {
        try {
            return context.getBodyAsJson().put(ID_ACTION, action());
        } catch (DecodeException e) {
            return new JsonObject().put(ID_ACTION, request.path());
        }
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }

    private void send(HttpServerRequest request, HttpResponseStatus status) {
        request.response().setStatusCode(status.code()).end();
    }

    private void send(HttpServerRequest request, Buffer buffer) {
        request.response().end(buffer);
    }

    private void send(HttpServerRequest request, Object object) {
        request.response().end(Buffer.buffer(object.toString()));
    }
}
