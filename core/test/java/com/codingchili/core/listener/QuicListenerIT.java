package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SecuritySettingsTest;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.transport.QuicListener;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Test cases for the QUIC transport.
 */
@RunWith(VertxUnitRunner.class)
public class QuicListenerIT extends ListenerTestCases {
    private static final int TOO_LARGE = 4096;
    private static final String PUSHED = "pushed";

    public QuicListenerIT() {
        super(QuicListener::new);
    }

    @BeforeClass
    public static void setUpKeystore() {
        Configurations.security().addKeystore()
                .setPath(SecuritySettingsTest.KEYSTORE_JKS)
                .setPassword(SecuritySettingsTest.PWD)
                .build();
    }

    @Override
    protected void configure(ListenerSettings settings) {
        // QUIC always requires TLS.
        settings.setKeystore(SecuritySettingsTest.KEY_JKS);
    }

    @Override
    public void sendRequest(ResponseListener listener, JsonObject data) {
        send(data.toBuffer()).onSuccess(body -> handleBody(listener, body));
    }

    private QuicClient client(String protocol) {
        // allow the server to open unidirectional streams, required to receive server push.
        QuicClientConfig config = new QuicClientConfig()
                .setTransportConfig(QuicConfig.forClient()
                        .setInitialMaxStreamsUni(16)
                        .setInitialMaxStreamDataUni(65_536));

        return context.vertx().createQuicClient(config, new ClientSSLOptions()
                .setTrustAll(true)
                .setHostnameVerificationAlgorithm("")
                .setApplicationLayerProtocols(List.of(protocol)));
    }

    private Future<QuicConnection> connect() {
        return client(CoreStrings.DEFAULT_QUIC_PROTOCOL).connect(port, HOST);
    }

    /**
     * Opens a new stream, writes the request and ends the stream.
     *
     * @return a future completed with the response written on the same stream.
     */
    private Future<Buffer> send(Buffer request) {
        return connect().compose(connection -> send(connection, request));
    }

    private Future<Buffer> send(QuicConnection connection, Buffer request) {
        return connection.openStream().compose(stream -> {
            Buffer response = Buffer.buffer();
            Promise<Buffer> promise = Promise.promise();
            stream.handler(response::appendBuffer);
            stream.endHandler(end -> promise.tryComplete(response));
            // the server sends STOP_SENDING for rejected requests, writes may fail.
            stream.exceptionHandler(e -> {});
            stream.end(request);
            return promise.future();
        });
    }

    private JsonObject ping() {
        return new JsonObject()
                .put(CoreStrings.PROTOCOL_TARGET, NODE_ROUTER)
                .put(CoreStrings.PROTOCOL_ROUTE, CoreStrings.ID_PING);
    }

    @Test
    public void testMultipleStreamsOneConnection(TestContext test) {
        connect().compose(connection -> Future.all(
                        send(connection, ping().toBuffer()),
                        send(connection, ping().toBuffer()),
                        send(connection, ping().toBuffer())))
                .onComplete(test.asyncAssertSuccess(all -> {
                    for (int i = 0; i < all.size(); i++) {
                        Buffer body = all.resultAt(i);
                        test.assertEquals(ResponseStatus.ACCEPTED.name(),
                                body.toJsonObject().getString(CoreStrings.PROTOCOL_STATUS));
                    }
                }));
    }

    @Test
    public void testLargeRequestRejected(TestContext test) {
        JsonObject request = ping().put("data", "x".repeat(TOO_LARGE));

        send(request.toBuffer()).onComplete(test.asyncAssertSuccess(body -> {
            test.assertEquals(ResponseStatus.BAD.name(),
                    body.toJsonObject().getString(CoreStrings.PROTOCOL_STATUS));
        }));
    }

    @Test
    public void testMalformedRequestRejected(TestContext test) {
        send(Buffer.buffer("{not json")).onComplete(test.asyncAssertSuccess(body -> {
            test.assertEquals(ResponseStatus.BAD.name(),
                    body.toJsonObject().getString(CoreStrings.PROTOCOL_STATUS));
        }));
    }

    @Test
    public void testAlpnMismatchFails(TestContext test) {
        client("not-" + CoreStrings.DEFAULT_QUIC_PROTOCOL).connect(port, HOST)
                .onComplete(test.asyncAssertFailure());
    }

    @Test
    public void testInsecureListenerFails(TestContext test) {
        ListenerSettings settings = new ListenerSettings()
                .setPort(0)
                .setSecure(false);

        context.listener(() -> new QuicListener().settings(settings).handler(new TestHandler()))
                .onComplete(test.asyncAssertFailure(e ->
                        test.assertTrue(e.getMessage().contains("QUIC requires TLS"))));
    }

    @Test
    public void testServerPushOnConnection(TestContext test) {
        Async async = test.async();
        ListenerSettings settings = new ListenerSettings()
                .setPort(0)
                .setKeystore(SecuritySettingsTest.KEY_JKS);

        CoreHandler<Request> pushing = new TestHandler() {
            @Override
            public void handle(Request request) {
                request.connection().write(new JsonObject().put(PUSHED, true));
                request.accept();
            }
        };

        context.listener(() -> new QuicListener().settings(settings).handler(pushing))
                .compose(deployed -> client(CoreStrings.DEFAULT_QUIC_PROTOCOL)
                        .connect(settings.getListenPorts().iterator().next(), HOST))
                .onComplete(test.asyncAssertSuccess(connection -> {
                    // server initiated messages arrive on unidirectional streams.
                    connection.streamHandler(stream -> {
                        Buffer pushed = Buffer.buffer();
                        stream.handler(pushed::appendBuffer);
                        stream.endHandler(end -> {
                            test.assertFalse(stream.isBidirectional());
                            test.assertTrue(pushed.toJsonObject().getBoolean(PUSHED));
                            async.complete();
                        });
                    });
                    send(connection, ping().toBuffer());
                }));
    }
}
