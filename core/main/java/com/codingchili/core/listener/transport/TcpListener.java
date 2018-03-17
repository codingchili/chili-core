package com.codingchili.core.listener.transport;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.util.function.Supplier;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;
import com.codingchili.core.protocol.Response;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * TCP listener implementation.
 */
public class TcpListener implements CoreListener {
    private Supplier<ListenerSettings> settings = ListenerSettings::getDefaultSettings;
    private RequestProcessor processor;
    private CoreContext core;
    private CoreHandler handler;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        handler.init(core);
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
        this.processor = new RequestProcessor(core, handler);

        core.vertx().createNetServer(settings.get().getHttpOptions(core))
                .connectHandler(socket -> {
                    Connection connection = connected(socket);

                    // handle incoming data.
                    socket.handler(data -> packet(connection, data));

                    // close the connection.
                    socket.closeHandler((v) -> connection.close());

                }).listen(settings.get().getPort(), getBindAddress(), listen -> {
            if (listen.succeeded()) {
                settings.get().addListenPort(listen.result().actualPort());
                handler.start(start);
            } else {
                start.fail(listen.cause());
            }
        });
    }

    public Connection connected(NetSocket socket) {
        return new Connection((msg) -> {
            socket.write(Response.convert(msg).toBuffer());
        }, socket.writeHandlerID());
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    private void packet(Connection connection, Buffer data) {
        processor.submit(() -> new TcpRequest(connection, data, settings.get()));
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.get().getPort();
    }
}
