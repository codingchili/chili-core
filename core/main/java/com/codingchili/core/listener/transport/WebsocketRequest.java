package com.codingchili.core.listener.transport;

import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 * <p>
 * Websocket request object.
 */
class WebsocketRequest implements Request {
    private int size;
    private ServerWebSocket socket;
    private ListenerSettings settings;
    private JsonObject data;

    WebsocketRequest(ServerWebSocket socket, Buffer buffer, ListenerSettings settings) {
        this.size = buffer.length();
        this.socket = socket;
        this.settings = settings;
        this.data = buffer.toJsonObject();
    }

    @Override
    public void write(Object object) {
        if (object instanceof Buffer) {
            socket.write((Buffer) object);
        } else {
            socket.write(Buffer.buffer(Serializer.pack(object)));
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
}
