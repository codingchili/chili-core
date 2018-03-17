package com.codingchili.core.listener;

import com.codingchili.core.context.*;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.testing.ContextMock;
import com.codingchili.core.testing.EmptyRequest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;
import static com.codingchili.core.files.Configurations.system;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * @author Robin Duda
 * <p>
 * Tests the cluster listener.
 */
@RunWith(VertxUnitRunner.class)
public class ClusterListenerTest {
    private static final String address = ClusterListenerTest.class.getName();
    private static final String TEST_MESSAGE = "{}";
    private static final String REPLY_ADDRESS = "clusterlistener-test";
    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.SECONDS);
    private ContextMock context;
    private TestHandler handler;
    private CoreListener cluster;
    private String deployment;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        this.context = new ContextMock();
        this.handler = new TestHandler(context, REPLY_ADDRESS);
        this.cluster = new ClusterListener().handler(handler).settings(ListenerSettings::new);

        context.listener(() -> cluster).setHandler(done -> {
            if (done.failed()) {
                done.cause().printStackTrace();
            }
            test.assertTrue(done.succeeded());
            this.deployment = done.result();
            async.complete();
        });
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void deployHandlerTest(TestContext test) throws CoreException {
        Async async = test.async();
        context.bus().consumer(REPLY_ADDRESS, event -> async.complete());
        handler.handle(new EmptyRequest());
    }

    @Test
    public void handlerOnStartCalled(TestContext test) throws CoreException {
        test.assertTrue(handler.startCalled);
    }

    @Test
    public void handlerOnStopCalled(TestContext test) throws CoreException {
        handler.setStopHandler(test.async(system().getListeners()));
        context.stop(deployment);
    }

    @Test
    public void testWriteBuffer(TestContext test) {
        Buffer buffer = Buffer.buffer("test");
        sendAndReply(buffer, (res) -> {
            test.assertEquals(buffer, res);
        }, test.async());
    }

    @Test
    public void testWriteCollection(TestContext test) {
        List<POJO> list = new ArrayList<>();
        list.add(new POJO().setStatus("test"));
        sendAndReply(list,  res -> {
            test.assertEquals(Serializer.json(list).put(PROTOCOL_STATUS, ACCEPTED), res);
        }, test.async());
    }

    @Test
    public void testWriteJsonObject(TestContext test) {
        JsonObject json = new JsonObject().put("testing", true);
        sendAndReply(json, res -> {
            test.assertEquals(json, res);
        }, test.async());
    }

    @Test
    public void testWritePOJO(TestContext test) {
        POJO pojo = new POJO().setStatus("accepted");

        sendAndReply(pojo, res -> {
            test.assertEquals(Serializer.json(pojo), res);
        }, test.async());
    }

    private <T> void sendAndReply(T object, Consumer<Object> assertion, Async async) {
        context.bus().consumer(address, msg -> {
            ClusterHelper.reply(msg, object);
        });

        context.bus().send(address, new JsonObject(), msg -> {
            assertion.accept(msg.result().body());
            async.complete();
        });
    }

    private class POJO {
        private String status;


        public String getStatus() {
            return status;
        }

        public POJO setStatus(String status) {
            this.status = status;
            return this;
        }
    }

    private class TestHandler implements CoreHandler {
        private boolean startCalled = false;
        private CoreContext context;
        private String address;
        private Async stop;

        TestHandler(CoreContext context, String address) {
            this.context = context;
            this.address = address;
        }

        @Override
        public void handle(Request request) {
            context.bus().send(REPLY_ADDRESS, TEST_MESSAGE);
        }

        @Override
        public String address() {
            return address;
        }

        @Override
        public void stop(Future<Void> future) {
            if (stop != null) {
                stop.countDown();
            }
            future.complete();
        }

        @Override
        public void start(Future<Void> future) {
            startCalled = true;
            future.complete();
        }

        void setStopHandler(Async stop) {
            this.stop = stop;
        }
    }
}
