package com.codingchili.core.listener.transport;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.util.function.Supplier;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;

import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 *         <p>
 *         TCP listener implementation.
 */
public class TcpListener implements CoreListener {
    private CoreContext core;
    private CoreHandler handler;
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
        core.vertx().createNetServer().connectHandler(handler -> {
            handler.handler(data -> packet(handler, data));
        }).listen(settings.get().getPort(), getBindAddress(), listen -> {
            if (listen.succeeded()) {
                settings.get().addListenPort(listen.result().actualPort());
                handler.start(start);
            } else {
                start.fail(listen.cause());
            }
        });
    }

    private void packet(NetSocket socket, Buffer data) {
        RequestProcessor.accept(core, handler, new TcpRequest(socket, data, settings.get()));
    }
}
