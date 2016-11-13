package com.codingchili.core.Context;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.Exception.CoreException;
import com.codingchili.core.Protocol.AbstractHandler;
import com.codingchili.core.Protocol.Request;
import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class DeployTest {
    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        Delay.initialize(new ContextMock(vertx));
    }

    @After
    public void tearDown(TestContext test) {
        /**
         * Re-enable once vertx-core #1625 is available.
         * vertx.close(test.asyncAssertSuccess());
         */
    }

    @Test
    public void testDeployService() {
        Deploy.service(new TestHandler(new ContextMock(vertx), ""));
    }

    @Test
    public void testDeployServiceWithAsyncResult(TestContext test) {
        Async async = test.async();

        Deploy.service(new TestHandler(new ContextMock(vertx), ""), deploy -> {
            if (deploy.succeeded()) {
                async.complete();
            } else {
                test.fail(deploy.cause());
            }
        });
    }

    private class TestHandler extends AbstractHandler<ContextMock> {

        TestHandler(ContextMock context, String address) {
            super(context, address);
        }

        @Override
        public void handle(Request request) throws CoreException {
            //
        }

        @Override
        public void start(Future<Void> future) {
            future.complete();
        }

        @Override
        public void stop(Future<Void> future) {
            future.complete();
        }
    }
}
