package com.codingchili.core.Logging;

import com.codingchili.core.Configuration.ConfigMock;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Logging.Configuration.LogProvider;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Logging.Controller.LogHandler;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Shared.RequestMock;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Protocols.ResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.ID_TOKEN;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the logging server.
 */
@RunWith(VertxUnitRunner.class)
public class LogHandlerTest {
    private TokenFactory factory;
    private LogHandler handler;
    private LogServerSettings settings;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        settings = new ConfigMock.LogServerSettingsMock();
        LogProvider provider = new ProviderMock();
        factory = new TokenFactory(settings.getSecret());
        handler = new LogHandler(provider);
    }

    @Test
    public void logMessage(TestContext context) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, getToken());
    }

    @Test
    public void failLogMessageWhenInvalidToken(TestContext context) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
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