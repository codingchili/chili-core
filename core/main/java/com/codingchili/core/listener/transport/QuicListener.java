package com.codingchili.core.listener.transport;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Response;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;
import com.codingchili.core.protocol.exception.RequestValidationException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * QUIC listener implementation, uses QUIC directly without HTTP/3.
 * <p>
 * Each request is sent on its own bidirectional stream: the client writes a JSON request and ends
 * its side of the stream, the response is written back on the same stream which is then ended.
 * Writes to the {@link Connection} outside of a request (server push) are sent on a new
 * unidirectional stream per message, clients must allow the server to open unidirectional
 * streams to receive them (QUIC initial_max_streams_uni and initial_max_stream_data_uni).
 * <p>
 * QUIC always uses TLS 1.3: the listener must be secure, and clients must negotiate the
 * {@link ListenerSettings#getQuicProtocol()} application protocol using ALPN.
 */
public class QuicListener implements CoreListener, DeploymentAware {
    /**
     * Application error code sent with STOP_SENDING when a request exceeds the max request size.
     */
    public static final int ERROR_REQUEST_TOO_LARGE = 0x1;
    /**
     * Application error code sent with STOP_SENDING when a client opens a unidirectional stream.
     */
    public static final int ERROR_UNIDIRECTIONAL_STREAM = 0x2;
    private ListenerSettings settings = ListenerSettings.getDefaultSettings();
    private CoreContext core;
    private CoreHandler handler;
    private QuicServer server;
    private Logger logger;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        this.logger = ListenerExceptionLogger.create(core, this, handler);
        handler.init(core);
    }

    @Override
    public Class<?> configType() {
        return QuicServerConfig.class;
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
        if (!settings.isSecure()) {
            start.fail(new CoreRuntimeException(getQuicRequiresSecure(toString())));
            return;
        }
        var handlerPromise = Promise.<Void>promise();

        handlerPromise.future()
                .compose(v -> listen())
                .onSuccess(address -> {
                    settings.addListenPort(address.port());
                    start.complete();
                })
                .onFailure(start::fail);

        handler.start(handlerPromise);
    }

    private Future<SocketAddress> listen() {
        ServerSSLOptions ssl = settings.getSecurity()
                .setApplicationLayerProtocols(List.of(settings.getQuicProtocol()));

        server = core.vertx().createQuicServer(settings.getQuic(), ssl)
                .exceptionHandler(logger::onError)
                .connectHandler(this::connected);

        return server.listen(settings.getPort(), getBindAddress());
    }

    private void connected(QuicConnection quic) {
        Connection connection = new Connection(message -> push(quic, message), UUID.randomUUID().toString())
                .setProperty(PROTOCOL_CONNECTION, quic.remoteAddress().host());

        quic.closeHandler(closed -> connection.runCloseHandlers());
        quic.streamHandler(stream -> stream(connection, stream));
    }

    private void push(QuicConnection quic, Object message) {
        quic.openStream(false)
                .compose(stream -> stream.end(Response.buffer(message)))
                .onFailure(logger::onError);
    }

    private void stream(Connection connection, QuicStream stream) {
        if (!stream.isBidirectional()) {
            // a request needs a stream to respond on.
            stream.abort(ERROR_UNIDIRECTIONAL_STREAM);
            return;
        }
        Buffer body = Buffer.buffer();
        AtomicBoolean rejected = new AtomicBoolean(false);

        stream.exceptionHandler(logger::onError);
        stream.handler(data -> {
            if (rejected.get()) {
                return;
            }
            if (body.length() + data.length() > settings.getMaxRequestBytes()) {
                // ask the client to stop sending the rest of the request, then answer.
                rejected.set(true);
                stream.abort(ERROR_REQUEST_TOO_LARGE);
                new QuicRequest(stream, connection, settings)
                        .error(new RequestPayloadSizeException(settings.getMaxRequestBytes()));
            } else {
                body.appendBuffer(data);
            }
        });
        stream.endHandler(end -> {
            if (!rejected.get()) {
                request(connection, stream, body);
            }
        });
    }

    private void request(Connection connection, QuicStream stream, Buffer body) {
        QuicRequest request;
        try {
            request = new QuicRequest(stream, connection, body, settings);
        } catch (RuntimeException e) {
            new QuicRequest(stream, connection, settings)
                    .error(new RequestValidationException(getRequestMalformed(e.getMessage())));
            return;
        }
        handler.handle(request);
    }

    @Override
    public void stop(Promise<Void> stop) {
        Future<Void> closed = (server == null) ? Future.succeededFuture() : server.close();
        closed.onComplete(done -> handler.stop(stop));
    }

    @Override
    public int instances() {
        // without load balancing every instance would try to bind the same UDP port.
        return settings.getQuic().isLoadBalanced() ? Configurations.system().getListeners() : 1;
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address() + " port :" +
                settings.getPort();
    }
}
