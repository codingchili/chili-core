package com.codingchili.router.controller.transport;

import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.model.WireType;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.*;

/**
 * @author Robin Duda
 *         <p>
 *         UDP request object.
 */
class UdpRequest extends BaseRequest {
    private DatagramPacket packet;
    private RouterContext context;
    private JsonObject data;

    UdpRequest(RouterContext context, DatagramPacket packet) {
        this.context = context;
        this.packet = packet;
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
        return context.getListener(WireType.UDP).getTimeout();
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
