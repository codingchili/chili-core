package com.codingchili.core.testing;

import com.codingchili.core.listener.Request;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.ID_BUFFER;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;
import static com.codingchili.core.protocol.ResponseStatus.*;

/**
 * @author Robin Duda
 * <p>
 * Tests the request mock, and partially ClusterRequest.
 * (there have been some problems with passing both buffers and json objects)
 */
@RunWith(VertxUnitRunner.class)
public class RequestMockTest {
    private static final String BUFFER_DATA = "<html>buffer example data </html>";
    private static final String PASS = "pass";
    private static final String TEST = "test";

    @Test
    public void testReplyWithJson(TestContext test) {
        Async async = test.async();

        Request request = RequestMock.get((response, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertEquals(PASS, response.getString(TEST));
            async.complete();
        });
        request.write(new JsonObject().put(TEST, PASS));
    }

    @Test
    public void testErrorStatusInJson(TestContext test) {
        Async async = test.async();

        Request request = RequestMock.get((response, status) -> {
            test.assertEquals(ERROR, status);
            async.complete();
        });
        request.write(new JsonObject().put(PROTOCOL_STATUS, ERROR));
    }

    @Test
    public void testReplyWithBuffer(TestContext test) {
        Async async = test.async();

        Request request = RequestMock.get((response, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertEquals(BUFFER_DATA, response.getString(ID_BUFFER));
            async.complete();
        });
        request.write(Buffer.buffer(BUFFER_DATA));
    }

    @Test
    public void testErrorStatusInBuffer(TestContext test) {
        Async async = test.async();

        Request request = RequestMock.get((response, status) -> {
            test.assertEquals(UNAUTHORIZED, status);
            async.complete();
        });
        request.write(Buffer.buffer(new JsonObject().put(PROTOCOL_STATUS, UNAUTHORIZED).encode()));
    }
}
