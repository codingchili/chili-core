package com.codingchili.core.listener.transport;

import io.vertx.core.Future;
import io.vertx.core.datagram.DatagramPacket;

import java.util.function.Supplier;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;

import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 *         <p>
 *         UDP transport listener.
 */
public class UdpListener implements CoreListener {
    private CoreHandler handler;
    private CoreContext core;
    private Supplier<ListenerSettings> settings;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public CoreListener settings(Supplier<ListenerSettings> settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Future<Void> start) {
        core.vertx().createDatagramSocket().listen(settings.get().getPort(), getBindAddress(), listen -> {
            if (listen.succeeded()) {
                settings.get().addListenPort(listen.result().localAddress().port());
                listen.result().handler(this::handle);
                handler.start(start);
            } else {
                start.fail(listen.cause());
            }
        });
    }

    private void handle(DatagramPacket connection) {
        RequestProcessor.accept(core, handler, new UdpRequest(core, settings.get(), connection));
    }
}
