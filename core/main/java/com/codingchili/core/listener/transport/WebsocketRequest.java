package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.BaseRequest;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 *         <p>
 *         Websocket request object.
 */
class WebsocketRequest extends BaseRequest {
    private int size;
    private ServerWebSocket socket;
    private ListenerSettings settings;
    private JsonObject data;


    WebsocketRequest(ServerWebSocket socket, Buffer buffer, ListenerSettings settings) {
        this.size = buffer.length();
        this.socket = socket;
        this.data = buffer.toJsonObject();
        this.settings = settings;
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
