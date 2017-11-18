package com.codingchili.core.listener.transport;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.RequestProcessor;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

import java.util.function.Supplier;

import static com.codingchili.core.configuration.CoreStrings.LOG_AT;
import static com.codingchili.core.configuration.CoreStrings.getBindAddress;

/**
 * @author Robin Duda
 * <p>
 * Websocket transport listener.
 */
public class WebsocketListener implements CoreListener {
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

        core.vertx().createHttpServer(settings.get().getHttpOptions(core))
                .websocketHandler(socket -> {
                    socket.handler(data -> handle(socket, data));
                }).requestHandler(rest -> {
            rest.response().setStatusCode(HttpResponseStatus.NOT_IMPLEMENTED.code()).end();
        }).listen(settings.get().getPort(), getBindAddress(), listen -> {
            if (listen.succeeded()) {
                settings.get().addListenPort(listen.result().actualPort());
                handler.start(start);
            } else {
                start.fail(listen.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    private void handle(ServerWebSocket socket, Buffer buffer) {
        processor.submit(new WebsocketRequest(socket, buffer, settings.get()));
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.get().getPort();
    }
}
