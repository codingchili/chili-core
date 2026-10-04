package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.QuicStream;

import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.AbstractRequest;
import com.codingchili.core.protocol.Response;

/**
 * QUIC request implementation, each request is read from and answered on its own stream.
 */
public class QuicRequest extends AbstractRequest {
    private final Connection connection;
    private final ListenerSettings settings;
    private final QuicStream stream;
    private final JsonObject data;
    private final int size;

    /**
     * @param stream     the stream the request was received on, the response is written to it.
     * @param connection the connection the stream belongs to.
     * @param buffer     the complete request body.
     * @param settings   the settings of the listener that received the request.
     */
    public QuicRequest(QuicStream stream, Connection connection, Buffer buffer, ListenerSettings settings) {
        this.stream = stream;
        this.connection = connection;
        this.settings = settings;
        this.size = buffer.length();
        this.data = buffer.toJsonObject();
    }

    /**
     * Creates a request without a body, used to respond with an error when the body is rejected.
     *
     * @param stream     the stream the request was received on, the response is written to it.
     * @param connection the connection the stream belongs to.
     * @param settings   the settings of the listener that received the request.
     */
    public QuicRequest(QuicStream stream, Connection connection, ListenerSettings settings) {
        this.stream = stream;
        this.connection = connection;
        this.settings = settings;
        this.size = 0;
        this.data = new JsonObject();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void write(Object object) {
        // one response per request: ending the stream tells the client the response is complete.
        stream.end(Response.buffer(object));
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
