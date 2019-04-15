package com.codingchili.core.listener.transport;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.function.Supplier;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;
import com.codingchili.core.protocol.Response;
import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Websocket transport listener.
 */
public class WebsocketListener implements CoreListener {
    private ListenerSettings settings = ListenerSettings.getDefaultSettings();
    private CoreContext core;
    private CoreHandler handler;

    @Override
    public void init(CoreContext core) {
        this.core = core;
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
    public void start(Future<Void> start) {
        listen(start);
    }

    private void listen(Future<Void> start) {
        Router router = Router.router(core.vertx());
        router.route().handler(BodyHandler.create());
        RestHelper.addHeaders(router, settings.isSecure());

        router.routeWithRegex(".*").handler(request -> {
            // handle all attempts at performing a HTTP request.
            request.response()
                    .setStatusCode(HttpResponseStatus.ACCEPTED.code())
                    .end(new JsonObject()
                            .put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED)
                            .put(ID_MESSAGE, CoreStrings.getRestNotSupportedByWebsocketListener())
                            .encodePrettily());
        });

        core.vertx().createHttpServer(settings.getHttpOptions())
                .websocketHandler(socket -> {
                    Connection connection = connected(socket);

                    // write data to the connection.
                    socket.handler(data -> handle(connection, data));

                    // close the connection on disconnect.
                    socket.closeHandler(closed -> connection.runCloseHandlers());

                }).requestHandler(router)
                .listen(settings.getPort(), getBindAddress(), listen -> {
                    if (listen.succeeded()) {
                        settings.addListenPort(listen.result().actualPort());
                        handler.start(start);
                    } else {
                        start.fail(listen.cause());
                    }
                });
    }

    private Connection connected(ServerWebSocket socket) {
        boolean isBinary = settings.isBinaryWebsockets();
        return new Connection((msg) -> {
            Buffer buffer = Response.buffer(msg);
            if (isBinary) {
                socket.write(buffer);
            } else {
                socket.writeTextMessage(buffer.toString());
            }
        }, socket.textHandlerID()).setProperty(PROTOCOL_CONNECTION, socket.remoteAddress().host());
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    private void handle(Connection connection, Buffer buffer) {
        handler.handle(new WebsocketRequest(connection, buffer, settings));
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.getPort();
    }
}