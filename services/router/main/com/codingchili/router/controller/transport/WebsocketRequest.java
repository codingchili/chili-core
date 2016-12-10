package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.ListenerSettings;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Token;

/**
 * @author Robin Duda
 *
 * Websocket request object.
 */
class WebsocketRequest extends BaseRequest {
    private ServerWebSocket socket;
    private ListenerSettings settings;
    private JsonObject data;


    WebsocketRequest(ServerWebSocket socket, Buffer buffer, ListenerSettings settings) {
        this.socket = socket;
        this.data = buffer.toJsonObject();
        this.settings = settings;
    }

    @Override
    protected void send(ResponseStatus status, Throwable exception) {
        socket.write(Protocol.response(status, exception));
    }

    @Override
    protected void send(ResponseStatus status) {
        socket.write(Protocol.response(status));
    }

    @Override
    public void write(Object object) {
        socket.write(Buffer.buffer(Serializer.pack(object)));
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }
}
