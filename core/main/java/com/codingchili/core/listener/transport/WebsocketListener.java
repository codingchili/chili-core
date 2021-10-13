package com.codingchili.core.listener.transport;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Response;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Websocket transport listener.
 */
public class WebsocketListener implements CoreListener {
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
        listen(start);
    }

    private void listen(Promise<Void> start) {
        Router router = Router.router(core.vertx());
        router.route().handler(BodyHandler.create()
                .setBodyLimit(settings.getMaxRequestBytes())
                .setHandleFileUploads(false)
        );
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
                .exceptionHandler(logger::onError)
                .webSocketHandler(socket -> {
                    Connection connection = connected(socket);

                    socket.handler(data -> handle(connection, data));
                    socket.closeHandler(closed -> connection.runCloseHandlers());
                    socket.exceptionHandler(logger::onError);

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
        }, socket.textHandlerID())
                .setProperty(PROTOCOL_CONNECTION, socket.remoteAddress().host());
    }

    private void handle(Connection connection, Buffer buffer) {
        var request = new WebsocketRequest(connection, buffer, settings);

        if (buffer.length() <= settings.getMaxRequestBytes()) {
            handler.handle(request);
        } else {
            request.error(new RequestPayloadSizeException(settings.getMaxRequestBytes()));
        }
    }

    @Override
    public void stop(Promise<Void> stop) {
        handler.stop(stop);
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.getPort();
    }
}