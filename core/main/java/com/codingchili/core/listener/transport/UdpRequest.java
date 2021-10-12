package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Response;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_CONNECTION;

/**
 * UDP request object.
 */
public class UdpRequest implements Request {
    private Connection connection;
    private final DatagramPacket packet;
    private final CoreContext context;
    private final ListenerSettings settings;
    private final int size;
    private JsonObject data;
    private DatagramSocket socket;

    public UdpRequest(CoreContext context, ListenerSettings settings, DatagramPacket packet) {
        this.size = packet.data().length();
        this.context = context;
        this.settings = settings;
        this.packet = packet;
    }

    @Override
    public void write(Object message) {
        write(message, true);
    }

    private void write(Object message, boolean reflectHeaders) {
        Buffer buffer;

        // only include the request headers when replying to a request - not on a connection.
        if (reflectHeaders) {
            buffer = Response.buffer(target(), route(), message);
        } else {
            buffer = Response.buffer(message);
        }

        createOrGet().send(buffer,
                packet.sender().port(),
                packet.sender().host(), sent -> {
                    if (sent.failed()) {
                        throw new RuntimeException(sent.cause());
                    }
                });
    }

    private DatagramSocket createOrGet() {
        if (socket == null) {
            socket = context.vertx().createDatagramSocket();
        }
        return socket;
    }

    @Override
    public Connection connection() {
        if (connection == null) {
            connection = new Connection((message) -> this.write(message, false), token().getDomain())
                    .setProperty(PROTOCOL_CONNECTION, packet.sender().host());
        }
        return connection;
    }

    @Override
    public JsonObject data() {
        if (data == null) {
            data = packet.data().toJsonObject();
        }
        return data;
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }

    @Override
    public int maxSize() {
        return settings.getMaxRequestBytes();
    }

    @Override
    public int size() {
        return size;
    }
}