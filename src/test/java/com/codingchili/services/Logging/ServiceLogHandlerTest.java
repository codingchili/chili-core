package com.codingchili.services.Logging;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.services.Shared.Strings;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Protocol.ResponseStatus;
import com.codingchili.core.Testing.RequestMock;
import com.codingchili.core.Testing.ResponseListener;

import com.codingchili.services.Logging.Configuration.LogContext;
import com.codingchili.services.Logging.Configuration.LogServerSettings;
import com.codingchili.services.Logging.Controller.ServiceLogHandler;

import static com.codingchili.services.Logging.Configuration.LogServerSettings.PATH_LOGSERVER;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the logging server.
 */
@RunWith(VertxUnitRunner.class)
public class ServiceLogHandlerTest {
    private ServiceLogHandler handler;
    private LogContext context;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        LogServerSettings settings = Configurations.get(PATH_LOGSERVER, LogServerSettings.class);
        context = new ContextMock(settings, Vertx.vertx());
        handler = new ServiceLogHandler<>(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void logMessage(TestContext context) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, new JsonObject());
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.process(RequestMock.get(action, listener, data));
    }
}