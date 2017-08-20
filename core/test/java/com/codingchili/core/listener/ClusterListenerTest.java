package com.codingchili.core.listener;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.testing.ContextMock;
import com.codingchili.core.testing.EmptyRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the cluster listener.
 */
@RunWith(VertxUnitRunner.class)
public class ClusterListenerTest {
    private static final String TEST_MESSAGE = "{}";
    private static final String REPLY_ADDRESS = "clusterlistener-test";
    private ContextMock context;
    private TestHandler handler;
    private CoreListener cluster;
    private String deployment;

    @Rule
    public Timeout timeout = new Timeout(8, TimeUnit.SECONDS);

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        this.context = new ContextMock(Vertx.vertx());
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
        context.vertx().close(test.asyncAssertSuccess());
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

    private class TestHandler implements CoreHandler {
        private boolean startCalled = false;
        private ServiceContext context;
        private String address;
        private Async stop;

        TestHandler(ServiceContext context, String address) {
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
