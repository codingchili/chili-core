package com.codingchili.core.Context;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
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

    @Test
    public void testDeployService() {
        Deploy.service(new TestHandler(new ContextMock(Vertx.vertx()), ""));
    }

    @Test
    public void testDeployServiceWithAsyncResult(TestContext test) {
        Async async = test.async();

        Deploy.service(new TestHandler(new ContextMock(Vertx.vertx()), ""), deploy -> async.complete());
    }

    private class TestHandler extends AbstractHandler<ContextMock> {

        TestHandler(ContextMock context, String address) {
            super(context, address);
        }

        @Override
        public void handle(Request request) throws CoreException {
            //
        }
    }
}
