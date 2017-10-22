package com.codingchili.logging.controller;

import com.codingchili.common.Strings;
import com.codingchili.core.protocol.ResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Contains test cases for use by client- and service log handlers.
 */
@RunWith(VertxUnitRunner.class)
public class ClientLogHandlerTest extends SharedLogHandlerTest {

    public ClientLogHandlerTest() {
        super();
        handler = new ClientLogHandler(context);
    }

    @Test
    public void failLogMessageWhenInvalidToken(TestContext test) {
        Async async = test.async();

        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
            async.complete();
        }, getLogMessage());
    }
}
