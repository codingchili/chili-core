package com.codingchili.router.controller.transport;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;

import com.codingchili.core.protocol.*;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.model.Endpoint;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *
 * HTTP/REST request object.
 */
class RestRequest extends BaseRequest {
    private RoutingContext context;
    private HttpServerRequest request;
    private ListenerSettings settings;
    private JsonObject data = new JsonObject();

    RestRequest(RoutingContext context, HttpServerRequest request, ListenerSettings settings) {
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

        if (!data.containsKey(PROTOCOL_ROUTE)) {
            data.put(PROTOCOL_ROUTE, context.request().path());
        }

        if (!data.containsKey(PROTOCOL_TARGET)) {
            data.put(PROTOCOL_TARGET, NODE_WEBSERVER);
        }
    }

    private void parseApi() {
        HashMap<String, Endpoint> api = settings.getApi();

        api.keySet().stream()
                .filter(route -> context.request().path().startsWith(route))
                .forEach(route -> {
                    Endpoint end = api.get(route);

                    if (end.getTarget() != null)
                        data.put(PROTOCOL_TARGET, end.getTarget());

                    if (end.getRoute() != null)
                        data.put(PROTOCOL_ROUTE, end.getRoute());
                });
    }

    private void parseHeaders() {
        context.request().headers().entries().stream()
                .forEach(entry -> {
                    data.put(entry.getKey(), entry.getValue());
                });
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
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }

    protected void send(ResponseStatus status, Throwable e) {
        request.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(Protocol.response(status, e));
    }

    protected void send(ResponseStatus status) {
        request.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(Protocol.response(status));
    }

    private void send(Buffer buffer) {
        request.response().setStatusCode(HttpResponseStatus.PARTIAL_CONTENT.code()).end(buffer);
    }

    private void send(Object object) {
        request.response().setStatusCode(HttpResponseStatus.OK.code()).end(Buffer.buffer(object.toString()));
    }
}
