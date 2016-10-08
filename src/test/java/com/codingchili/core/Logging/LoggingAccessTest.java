package com.codingchili.core.Logging;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Logging.Configuration.LogProvider;
import com.codingchili.core.Logging.Controller.LogHandler;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 */

@RunWith(VertxUnitRunner.class)
public class LoggingAccessTest {
    private LogHandler handler;

    @Before
    public void setUp() {
        LogProvider provider = new ProviderMock();
        handler = new LogHandler(provider);
    }

    @Test
    public void failLogMessageWhenInvalidToken(TestContext context) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(new LogRequestMock(action, listener, data));
    }
}
