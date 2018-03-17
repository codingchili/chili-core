package com.codingchili.core.listener.transport;

import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Response;

/**
 * @author Robin Duda
 * <p>
 * UDP request object.
 */
class UdpRequest implements Request {
    private int size;
    private DatagramPacket packet;
    private CoreContext context;
    private ListenerSettings settings;
    private JsonObject data;

    UdpRequest(CoreContext context, ListenerSettings settings, DatagramPacket packet) {
        this.size = packet.data().length();
        this.context = context;
        this.settings = settings;
        this.packet = packet;
    }

    @Override
    public void write(Object message) {
        context.vertx().createDatagramSocket()
                .send(Response.message(this, message).toBuffer(),
                        packet.sender().port(),
                        packet.sender().host(), sent -> {
                            if (sent.failed()) {
                                throw new RuntimeException(sent.cause());
                            }
                        });
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