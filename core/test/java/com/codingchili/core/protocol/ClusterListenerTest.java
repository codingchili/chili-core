package com.codingchili.core.protocol;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.testing.ContextMock;
import com.codingchili.core.testing.EmptyRequest;

/**
 * @author Robin Duda
 *
 * Tests the cluster listener.
 */
@RunWith(VertxUnitRunner.class)
public class ClusterListenerTest {
    private static final String TEST_MESSAGE = "{}";
    private static final String REPLY_ADDRESS = "clusterlistener-test";
    private ContextMock context;
    private TestHandler handler;
    private ClusterListener cluster;

    @Before
    public void setUp(TestContext test) {
        this.context = new ContextMock(Vertx.vertx());
        this.handler = new TestHandler<>(context, REPLY_ADDRESS);
        this.cluster = ClusterListener.with(handler);

        context.deploy(cluster, test.asyncAssertSuccess());
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
        handler.setStopHandler(test.async());
        cluster.stop(Future.future());
    }

    private class TestHandler<T extends ServiceContext> extends AbstractHandler<T> {
        private boolean startCalled = false;
        private Async stop;

        TestHandler(T context, String address) {
            super(context, address);
        }

        @Override
        public void init(CoreContext core) {
        }

        @Override
        public void handle(Request request) throws CoreException {
            context().bus().send(REPLY_ADDRESS, TEST_MESSAGE);
        }

        @Override
        public void stop(Future<Void> future) {
            if (stop != null) {
                stop.complete();
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
