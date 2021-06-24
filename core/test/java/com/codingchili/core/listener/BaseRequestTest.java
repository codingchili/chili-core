package com.codingchili.core.listener;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.UnmappedException;
import com.codingchili.core.security.Token;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.listener.Request.TARGET_UNDEFINED;
import static com.codingchili.core.protocol.ResponseStatus.*;

/**
 * Test cases for request implementations.
 */
@RunWith(VertxUnitRunner.class)
public class BaseRequestTest {
    public static final String TEST = "test";
    public static final String PASS = "pass";
    private static final String ROUTE = "route";
    private static final String TARGET = "/";
    private static final int TIMEOUT = 50;
    private Token token = new Token().setKey("key").setExpiry(10L);
    private Request request = new RequestTest();
    private JsonObject response;
    private JsonObject data = new JsonObject()
            .put(PROTOCOL_ROUTE, ROUTE)
            .put(PROTOCOL_TARGET, TARGET)
            .put(ID_TOKEN, Serializer.json(token));

    @Test
    public void testRoute(TestContext test) {
        test.assertEquals(ROUTE, request.route());
    }

    @Test
    public void testTarget(TestContext test) {
        test.assertEquals(TARGET, request.target());
    }

    @Test
    public void TestToken(TestContext test) {
        test.assertEquals(token.getKey(), request.token().getKey());
        test.assertEquals(token.getExpiry(), request.token().getExpiry());
        test.assertEquals(token.getDomain(), request.token().getDomain());
    }

    @Test
    public void testEmptyTokenGeneratedAndDomainIsRandom(TestContext test) {
        data.remove(ID_TOKEN);
        test.assertNotNull(request.token());
        test.assertNotEquals(token, request.token());

        test.assertNotNull(request.token().getDomain());
        test.assertNotNull(request.token().getKey());

        // even if the domain is randomized - make sure its expired.
        test.assertEquals(0L, request.token().getExpiry());
    }

    @Test
    public void testRouteIsANYIfUnset(TestContext test) {
        data.remove(PROTOCOL_ROUTE);
        test.assertEquals(ANY, request.route());
    }

    @Test
    public void testWriteResponse(TestContext test) {
        request.write(new JsonObject().put(TEST, PASS));
        test.assertEquals(PASS, response.getString(TEST));
        test.assertEquals(ResponseStatus.ACCEPTED.toString(), response.getString(PROTOCOL_STATUS));
    }

    @Test
    public void testCannotWriteNonCoreException(TestContext test) {
        try {
            request.write(new Exception("cannot write this"));
            test.fail("test failed to throw exception when writing non-core exception.");
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testNonCoreErrorConvertedToUnmappedException(TestContext test) {
        Exception e = new Exception("cannot write this");
        request.error(e);
        test.assertEquals(ERROR.toString(), response.getString(PROTOCOL_STATUS));
        test.assertEquals(new UnmappedException(e).getMessage(), response.getString(PROTOCOL_MESSAGE));
    }

    @Test
    public void testRequestUnauthorized(TestContext test) {
        request.error(new CoreExceptionTest(PASS, ResponseStatus.UNAUTHORIZED));
        test.assertEquals(UNAUTHORIZED.toString(), response.getString(PROTOCOL_STATUS));
    }

    @Test
    public void testTargetIsUndefinedIfMissing(TestContext test) {
        data.remove(PROTOCOL_TARGET);
        test.assertEquals(TARGET_UNDEFINED, request.target());
    }

    @Test
    public void testAccept(TestContext test) {
        request.accept();
        test.assertEquals(ACCEPTED.toString(), response.getString(PROTOCOL_STATUS));
    }

    @Test
    public void testData(TestContext test) {
        test.assertEquals(data, request.data());
    }

    @Test
    public void testTimeout(TestContext test) {
        test.assertEquals(TIMEOUT, request.timeout());
    }

    private class RequestTest implements Request {
        @Override
        public void write(Object object) {
            if (object instanceof Exception && !(object instanceof CoreException)) {
                throw new RuntimeException("DING DONG cannot write non-core exception!");
            } else {
                if (object instanceof Buffer) {
                    response = ((Buffer) object).toJsonObject();
                } else {
                    response = Serializer.json(object);
                }
            }

            if (!response.containsKey(PROTOCOL_STATUS)) {
                response.put(PROTOCOL_STATUS, ACCEPTED);
            }
        }

        @Override
        public Connection connection() {
            return new Connection(this::write, "_test_ic");
        }

        @Override
        public JsonObject data() {
            return data;
        }

        @Override
        public int timeout() {
            return TIMEOUT;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private class CoreExceptionTest extends CoreException {
        CoreExceptionTest(String error, ResponseStatus status) {
            super(error, status);
        }
    }
}
