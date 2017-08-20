package com.codingchili.logging.controller;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.common.Strings;
import com.codingchili.core.protocol.ResponseStatus;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *
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
