package com.codingchili.core.listener.transport;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.*;
import com.codingchili.core.protocol.*;

/**
 * @author Robin Duda
 * <p>
 * HTTP/REST request object.
 */
class RestRequest implements Request {
    private HttpServerRequest request;
    private JsonObject data = new JsonObject();
    private ListenerSettings settings;
    private int size;

    RestRequest(RoutingContext context, ListenerSettings settings, HttpServerRequest request) {
        this.size = context.getBody().length();
        this.request = request;
        this.settings = settings;

        parseData(context);
        parseHeaders(context);
    }

    @Override
    public Connection connection() {
        // write the head first.
        request.response().setStatusCode(HttpResponseStatus.OK.code());

        final Connection connection = new Connection((object) -> {
            // write data without calling end - supports long polling.
            request.response().write(Buffer.buffer(object.toString()));
        }, request.netSocket().writeHandlerID());

        connection.onClose(() -> {
            // commit the response when the connection is (pre-)closed.
            request.response().end();
        });
        return connection;
    }

    private void parseData(RoutingContext context) {
        String body = context.getBodyAsString();
        if (body == null || body.length() == 0) {
            request.params().forEach(entry -> {
                data.put(entry.getKey(), entry.getValue());
            });
        } else {
            data = new JsonObject(body);
        }

        parseApi(context);

        if (!data.containsKey(CoreStrings.PROTOCOL_ROUTE)) {
            data.put(CoreStrings.PROTOCOL_ROUTE, context.request().path().replaceFirst("/", ""));
        }

        if (!data.containsKey(CoreStrings.PROTOCOL_TARGET)) {
            data.put(CoreStrings.PROTOCOL_TARGET, settings.getDefaultTarget());
        }
    }

    private void parseApi(RoutingContext context) {
        Map<String, Endpoint> api = settings.getApi();

        api.keySet().stream()
                .filter(route -> context.request().path().startsWith(route))
                .forEach(route -> {
                    Endpoint end = api.get(route);

                    if (end.getTarget() != null)
                        data.put(CoreStrings.PROTOCOL_TARGET, end.getTarget());

                    if (end.getRoute() != null)
                        data.put(CoreStrings.PROTOCOL_ROUTE, end.getRoute());
                });
    }

    private void parseHeaders(RoutingContext context) {
        context.request().headers().entries().forEach(entry ->
                data.put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void write(Object message) {
        send(Response.message(this, message).toBuffer());
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int maxSize() {
        return settings.getMaxRequestBytes();
    }

    protected void send(ResponseStatus status, Throwable e) {
        request.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(Response.error(this, status, e).encode());
    }

    protected void send(ResponseStatus status) {
        request.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(Response.status(this, status).encode());
    }

    private void send(Buffer buffer) {
        request.response().setStatusCode(HttpResponseStatus.OK.code()).end(buffer);
    }
}
