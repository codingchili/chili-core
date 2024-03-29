package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Response;

/**
 * TCP request implementation.
 */
public class TcpRequest implements Request {
    private Connection connection;
    private ListenerSettings settings;
    private JsonObject data;
    private int size;

    public TcpRequest(Connection connection, Buffer buffer, ListenerSettings settings) {
        this.size = buffer.length();
        this.connection = connection;
        this.settings = settings;
        this.data = buffer.toJsonObject();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void write(Object object) {
        connection.write(Response.buffer(object));
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
