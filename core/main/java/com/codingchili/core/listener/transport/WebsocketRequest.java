package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Response;

/**
 * @author Robin Duda
 * <p>
 * Websocket request object.
 */
public class WebsocketRequest implements Request {
    private int size;
    private Connection connection;
    private ListenerSettings settings;
    private JsonObject data;

    WebsocketRequest(Connection connection, Buffer buffer, ListenerSettings settings) {
        this.connection = connection;
        this.size = buffer.length();
        this.settings = settings;
        this.data = buffer.toJsonObject();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void write(Object object) {
        connection.write(Response.buffer(target(), route(), object));
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
