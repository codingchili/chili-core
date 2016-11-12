package com.codingchili.core.Logging;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.Context.SystemContext;
import com.codingchili.core.Testing.ContextMock;

import static com.codingchili.services.Shared.Strings.NODE_LOGGING;

/**
 * @author Robin Duda
 *
 * Verify that the remote logger is pushing events to a remote.
 */
@RunWith(VertxUnitRunner.class)
public class RemoteLoggerTest {
    private SystemContext context;

    @Before
    public void setUp() {
        context = new ContextMock(Vertx.vertx());
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testLogRemote(TestContext test) {
        mockNode(test.async());

        new RemoteLogger(context).log("text");
    }

    private void mockNode(Async async) {
        context.bus().consumer(NODE_LOGGING).handler(message -> {
            async.complete();
        });
    }
}
