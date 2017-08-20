package com.codingchili.core.listener.transport;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.BaseRequest;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.protocol.Serializer;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         <p>
 *         UDP request object.
 */
class UdpRequest extends BaseRequest {
    private int size;
    private DatagramPacket packet;
    private CoreContext context;
    private ListenerSettings listener;
    private JsonObject data;

    UdpRequest(CoreContext context, ListenerSettings listener, DatagramPacket packet) {
        this.size = packet.data().length();
        this.context = context;
        this.listener = listener;
        this.packet = packet;
    }

    @Override
    public void init() {
        this.data = packet.data().toJsonObject();
    }

    @Override
    public void write(Object object) {
        send(object);
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return listener.getTimeout();
    }

    @Override
    public int maxSize() {
        return listener.getMaxRequestBytes();
    }

    @Override
    public int size() {
        return size;
    }

    private void send(Object object) {
        if (object instanceof Buffer) {
            send((Buffer) object);
        } else {
            send(Buffer.buffer(Serializer.pack(object)));
        }
    }

    private void send(Buffer buffer) {
        context.vertx().createDatagramSocket()
                .send(buffer,
                        packet.sender().port(),
                        packet.sender().host(), sent -> {
                        });
    }
}
