package com.codingchili.logging;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.configuration.LogServerSettings;
import com.codingchili.logging.controller.ClientLogHandler;
import com.codingchili.common.Strings;

import static com.codingchili.common.Strings.ID_TOKEN;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class ClientLogHandlerTest {
    private TokenFactory factory;
    private ClientLogHandler handler;
    private LogContext context;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        LogServerSettings settings = new LogServerSettings();
        settings.setSecret(new byte[]{0x0});

        context = new ContextMock(settings, Vertx.vertx());
        factory = new TokenFactory(settings.getSecret());
        handler = new ClientLogHandler<>(context);
    }
    
    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void logMessage(TestContext test) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, getToken());
    }

    @Test
    public void failLogMessageWhenInvalidToken(TestContext test) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    private JsonObject getToken() {
        return new JsonObject().put(ID_TOKEN, Serializer.json(new Token(factory, "domain")));
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.process(RequestMock.get(action, listener, data));
    }
}
