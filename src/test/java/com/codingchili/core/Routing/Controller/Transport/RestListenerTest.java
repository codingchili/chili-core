package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Routing.Configuration.ListenerSettings;
import com.codingchili.core.Routing.Configuration.RouteProvider;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Routing.Controller.RouteHandler;
import com.codingchili.core.Routing.Model.WireType;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class RestListenerTest {
    private static final int PORT = 19797;
    private Vertx vertx;
    private RoutingSettings settings;
    private RouteProvider provider;
    private ListenerSettings listener;
    private String deployId;

    @Before
    public void setUp(TestContext context) {
        Async async = context.async();

        Vertx.clusteredVertx(new VertxOptions(), cluster -> {
            this.vertx = cluster.result();

            settings = new RoutingSettings();
            provider = new RouteProvider(vertx);

            ArrayList<ListenerSettings> transport = new ArrayList<>();

            listener = new ListenerSettings();
            listener.setMaxRequestBytes(10);
            listener.setPort(PORT);
            listener.setType(WireType.REST);
            transport.add(listener);

            settings.setTransport(transport);

            vertx.deployVerticle(new RestListener(new RouteHandler(provider), settings), deploy -> {
                deployId = deploy.result();
                async.complete();
            });
        });
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void testLargePacketRejected(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient().post(PORT, "localhost", "/", handler -> {
            context.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), handler.statusCode());
            async.complete();
        }).end(Buffer.buffer("request is larger than 10 bytes!!!"));
    }
}
