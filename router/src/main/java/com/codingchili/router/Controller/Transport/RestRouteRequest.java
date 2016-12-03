package com.codingchili.services.router.controller.transport;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;

import com.codingchili.core.protocol.Request;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;

import com.codingchili.services.router.configuration.ListenerSettings;
import com.codingchili.services.router.model.Endpoint;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 */
class RestRouteRequest implements Request {
    private RoutingContext context;
    private HttpServerRequest request;
    private ListenerSettings settings;
    private JsonObject data = new JsonObject();

    public RestRouteRequest() {
    }

    RestRouteRequest(RoutingContext context, HttpServerRequest request, ListenerSettings settings) {
        this.context = context;
        this.request = request;
        this.settings = settings;

        // Eager initialization; these values will always be used.
        parseData();
        parseHeaders();
    }

    private void parseData() {
        try {
            data = context.getBodyAsJson();
        } catch (DecodeException e) {
            request.params().forEach(entry -> {
                data.put(entry.getKey(), entry.getValue());
            });
        }

        parseApi();

        if (!data.containsKey(ID_ROUTE)) {
            data.put(ID_ROUTE, context.request().path());
        }

        if (!data.containsKey(ID_TARGET)) {
            data.put(ID_TARGET, NODE_WEBSERVER);
        }
    }

    private void parseApi() {
        HashMap<String, Endpoint> api = settings.getApi();

        api.keySet().stream()
                .filter(route -> context.request().path().startsWith(route))
                .forEach(route -> {
                    Endpoint end = api.get(route);

                    if (end.getTarget() != null)
                        data.put(ID_TARGET, end.getTarget());

                    if (end.getRoute() != null)
                        data.put(ID_ROUTE, end.getRoute());
                });
    }

    private void parseHeaders() {
        context.request().headers().entries().stream()
                .forEach(entry -> {
                    data.put(entry.getKey(), entry.getValue());
                });
    }

    @Override
    public void accept() {
        send(ResponseStatus.ACCEPTED);
    }

    @Override
    public void error(Throwable exception) {
        send(ResponseStatus.ERROR, exception);
    }

    @Override
    public void unauthorized(Throwable exception) {
        send(ResponseStatus.UNAUTHORIZED);
    }

    @Override
    public void missing(Throwable exception) {
        send(ResponseStatus.MISSING, exception);
    }

    @Override
    public void conflict(Throwable exception) {
        send(ResponseStatus.CONFLICT, exception);
    }

    @Override
    public void bad(Throwable exception) {
        send(ResponseStatus.BAD, exception);
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
    public String route() {
        return data().getString(ID_ROUTE);
    }

    @Override
    public String target() {
        return data().getString(ID_TARGET);
    }

    @Override
    public Token token() {
        return Serializer.unpack(data.getJsonObject(ID_TOKEN), Token.class);
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }

    private void send(ResponseStatus status, Throwable e) {
        request.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(new JsonObject()
                        .put(PROTOCOL_STATUS, status)
                        .put(ID_MESSAGE, e.getMessage())
                        .encode());
    }

    private void send(ResponseStatus status) {
        request.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(new JsonObject()
                        .put(PROTOCOL_STATUS, status)
                        .encode());
    }

    private void send(Buffer buffer) {
        request.response().setStatusCode(HttpResponseStatus.PARTIAL_CONTENT.code()).end(buffer);
    }

    private void send(Object object) {
        request.response().setStatusCode(HttpResponseStatus.OK.code()).end(Buffer.buffer(object.toString()));
    }
}
