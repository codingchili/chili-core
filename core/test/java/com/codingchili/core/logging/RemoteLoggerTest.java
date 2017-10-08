package com.codingchili.core.logging;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.testing.ContextMock;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.NODE_LOGGING;

/**
 * @author Robin Duda
 * <p>
 * Verify that the remote logger is pushing events to a remote.
 */
@RunWith(VertxUnitRunner.class)
public class RemoteLoggerTest {
    private CoreContext context;

    @Before
    public void setUp() {
        context = new ContextMock();
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void testLogRemote(TestContext test) {
        mockNode(test.async());
        new RemoteLogger(context, getClass()).log("text");
    }

    private void mockNode(Async async) {
        context.bus().consumer(NODE_LOGGING).handler(message -> {
            async.complete();
        });
    }
}
