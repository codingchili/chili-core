package com.codingchili.core.listener.transport;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.BaseRequest;
import com.codingchili.core.listener.Endpoint;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.ResponseStatus;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

/**
 * @author Robin Duda
 *         <p>
 *         HTTP/REST request object.
 */
class RestRequest extends BaseRequest {
    private RoutingContext context;
    private HttpServerRequest request;
    private JsonObject data = new JsonObject();
    private ListenerSettings settings;
    private int size;

    RestRequest(RoutingContext context, ListenerSettings settings, HttpServerRequest request) {
        this.size = context.getBody().length();
        this.context = context;
        this.request = request;
        this.settings = settings;
    }

    @Override
    public void init() {
        parseData();
        parseHeaders();
    }

    private void parseData() {
        String body = context.getBodyAsString();
        if (body == null || body.length() == 0) {
            request.params().forEach(entry -> {
                data.put(entry.getKey(), entry.getValue());
            });
        } else {
            data = new JsonObject(body);
        }

        parseApi();

        if (!data.containsKey(CoreStrings.PROTOCOL_ROUTE)) {
            data.put(CoreStrings.PROTOCOL_ROUTE, context.request().path().replaceFirst("/", ""));
        }

        if (!data.containsKey(CoreStrings.PROTOCOL_TARGET)) {
            data.put(CoreStrings.PROTOCOL_TARGET, settings.getDefaultTarget());
        }
    }

    private void parseApi() {
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

    private void parseHeaders() {
        context.request().headers().entries().forEach(entry ->
                data.put(entry.getKey(), entry.getValue()));
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
                .end(Protocol.response(status, e).encode());
    }

    protected void send(ResponseStatus status) {
        request.response().setStatusCode(HttpResponseStatus.OK.code())
                .end(Protocol.response(status).encode());
    }

    private void send(Buffer buffer) {
        request.response().setStatusCode(HttpResponseStatus.PARTIAL_CONTENT.code()).end(buffer);
    }

    private void send(Object object) {
        request.response().setStatusCode(HttpResponseStatus.OK.code()).end(Buffer.buffer(object.toString()));
    }
}
