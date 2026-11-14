package com.codingchili.core.listener.transport;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerConfig;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrameType;
import io.vertx.core.http.impl.websocket.WebSocketFrameImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.UUID;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.RestHelper;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.*;
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
    public Class<?> configType() {
        return HttpServerConfig.class;
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
        try {
            // fails the deployment when a trusted proxy is not valid.
            settings.proxies();
        } catch (CoreRuntimeException e) {
            start.fail(e);
            return;
        }
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

        var handlerPromise = Promise.<Void>promise();
        handlerPromise.future()
                .compose(v -> core.vertx().createHttpServer(settings.getHttpOptions(), settings.getSecurity())
                        .exceptionHandler(ListenerExceptionLogger.handler(logger))
                        .webSocketHandler(socket -> {
                            Connection connection = connected(socket);

                            socket.handler(data -> handle(connection, data));
                            socket.closeHandler(closed -> connection.runCloseHandlers());
                            socket.exceptionHandler(ListenerExceptionLogger.handler(logger));

                        }).requestHandler(router)
                        .listen(settings.getPort(), getBindAddress()))
                .onSuccess(result -> {
                    settings.addListenPort(result.actualPort());
                    start.complete();
                })
                .onFailure(start::fail);
        handler.start(handlerPromise);
    }

    private Connection connected(ServerWebSocket socket) {
        boolean isBinary = settings.isBinaryWebsockets();

        return new Connection((msg) -> {
            if (isBinary) {
                socket.write(Response.buffer(msg));
            } else {
                if (msg instanceof String) {
                    socket.writeTextMessage((String) msg);
                } else {
                    var buffer = Response.buffer(msg);
                    // create a text frame directly from a buffer.
                    socket.writeFrame(
                            new WebSocketFrameImpl(
                                    WebSocketFrameType.TEXT, buffer.getBytes(), true)
                    );
                }
            }
        }, UUID.randomUUID().toString())
                .closer(socket::close)
                .setProperty(PROTOCOL_CONNECTION, settings.proxies().client(
                        socket.remoteAddress().host(), socket.headers().getAll(PROTOCOL_FORWARDED_FOR)));
    }

    private void handle(Connection connection, Buffer buffer) {
        var limit = connection.maxRequestBytesOr(settings.getMaxRequestBytes());
        if (buffer.length() > limit) {
            // not decoded: answered from the size alone (the connection's limit, else the listener's).
            connection.write(Response.error(null, null, ResponseStatus.BAD, new RequestPayloadSizeException(limit)));
            return;
        }
        WebsocketRequest request;
        try {
            request = new WebsocketRequest(connection, buffer, settings);
        } catch (RuntimeException e) {
            // not json, or nested beyond the parser's limit: the client's mistake, answered and not logged.
            connection.write(Response.error(null, null, ResponseStatus.BAD, e));
            return;
        }
        handler.handle(request);
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