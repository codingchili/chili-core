package com.codingchili.logging.controller;

import com.codingchili.core.context.SystemContext;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.Vertx;
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

    public ServiceLogHandlerTest() {
        super();
        handler = new ServiceLogHandler(context);
    }

    @Before
    public void setUp() {
        context = new LogContext(new SystemContext());
        context.storage().clear(clear -> {
        });
        handler = new ServiceLogHandler(context);
    }
}