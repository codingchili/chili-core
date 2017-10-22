package com.codingchili.logging.controller;

import com.codingchili.core.context.SystemContext;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Tests the logging server.
 */
@RunWith(VertxUnitRunner.class)
public class ServiceLogHandlerTest extends SharedLogHandlerTest {
    private Future<Void> future = Future.future();

    public ServiceLogHandlerTest() {
        super();
        handler = new ServiceLogHandler(context);
    }

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        context = new LogContext(new SystemContext(), future);

        future.setHandler(done -> {
            context.storage().clear(clear -> {
                async.complete();
            });
            handler = new ServiceLogHandler(context);
        });
    }
}