package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Response;

/**
 * Websocket request object.
 */
public class WebsocketRequest implements Request {
    private final ListenerSettings settings;
    private final Connection connection;
    private final JsonObject data;
    private final int size;

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
        try {
            connection.write(Response.buffer(target(), route(), object));
        } catch (Exception e) {
            throw new CoreRuntimeException(e);
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
