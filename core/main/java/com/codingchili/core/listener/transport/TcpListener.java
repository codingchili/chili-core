package com.codingchili.core.listener.transport;

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Response;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * TCP listener implementation.
 */
public class TcpListener implements CoreListener {
    private ListenerSettings settings = ListenerSettings.getDefaultSettings();
    private CoreContext core;
    private CoreHandler handler;
    private Logger logger;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        this.logger = ListenerExceptionLogger.create(core, this, handler);
        handler.init(core);
    }

    @Override
    public CoreListener settings(ListenerSettings settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Promise<Void> start) {
        var handlerPromise = Promise.<Void>promise();

        handlerPromise.future().onSuccess((v) -> {
            core.vertx().createNetServer(settings.getHttpOptions())
                    .exceptionHandler(logger::onError)
                    .connectHandler(socket -> {
                        Connection connection = connected(socket);

                        socket.handler(data -> packet(connection, data));
                        socket.closeHandler((close) -> connection.runCloseHandlers());
                        socket.exceptionHandler(logger::onError);

                    }).listen(settings.getPort(), getBindAddress(), listen -> {
                        if (listen.succeeded()) {
                            settings.addListenPort(listen.result().actualPort());
                            start.complete();
                        } else {
                            start.fail(listen.cause());
                        }
                    });
        }).onFailure(start::fail);

        handler.start(handlerPromise);
    }

    public Connection connected(NetSocket socket) {
        return new Connection((msg) -> socket.write(Response.buffer(msg)), socket.writeHandlerID())
                .setProperty(PROTOCOL_CONNECTION, socket.remoteAddress().host());
    }

    @Override
    public void stop(Promise<Void> stop) {
        handler.stop(stop);
    }

    private void packet(Connection connection, Buffer data) {
        handler.handle(new TcpRequest(connection, data, settings));
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.getPort();
    }
}
