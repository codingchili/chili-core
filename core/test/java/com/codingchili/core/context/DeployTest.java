package com.codingchili.core.context;

import org.junit.*;
import org.junit.runner.*;

import com.codingchili.core.protocol.*;
import com.codingchili.core.protocol.Request;
import com.codingchili.core.testing.*;

import io.vertx.core.*;
import io.vertx.ext.unit.*;
import io.vertx.ext.unit.junit.*;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class DeployTest {
    private static final String ADDRESS = "address.node";
    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        Delay.initialize(new ContextMock(vertx));
    }

    @After
    public void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void testDeployService() {
        Deploy.service(new TestHandler(new ContextMock(vertx), ADDRESS));
    }

    @Test
    public void testDeployServiceWithAsyncResult(TestContext test) {
        Async async = test.async();
        TestHandler handler = new TestHandler(new ContextMock(vertx), ADDRESS);
        Deploy.service(handler, deploy -> {
            if (deploy.succeeded()) {
                test.assertEquals(ADDRESS, handler.address());
                test.assertEquals(ContextMock.NODE, handler.context().node());
                async.complete();
            } else {
                test.fail(deploy.cause());
            }
        });
    }

    private class TestHandler implements CoreHandler {
        private String address;
        private ContextMock context;

        TestHandler(ContextMock context, String address) {
            this.address = address;
            this.context = context;
        }

        @Override
        public void init(CoreContext core) {
            //
        }

        @Override
        public void handle(Request request) throws CoreException {
            //
        }

        @Override
        public ServiceContext context() {
            return context;
        }

        @Override
        public String address() {
            return address;
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
