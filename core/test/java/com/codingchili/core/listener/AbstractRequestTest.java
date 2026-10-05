package com.codingchili.core.listener;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.listener.transport.ClusterRequest;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.testing.RequestMock;

import static com.codingchili.core.configuration.CoreStrings.ID_TOKEN;

/**
 * Tests that the token of a request is parsed once.
 */
@RunWith(VertxUnitRunner.class)
public class AbstractRequestTest {
    private static final String ROUTE = "route";
    private static final String DOMAIN = "domain";
    private static final String KEY = "key";

    private ClusterRequest request(JsonObject data) {
        return RequestMock.get(ROUTE, (response, status) -> {
        }, data);
    }

    @Test
    public void testTokenIsParsedOnce(TestContext test) {
        Token token = new Token(DOMAIN).setKey(KEY).setExpiry(42);
        Request request = request(new JsonObject().put(ID_TOKEN, Serializer.json(token)));

        Assert.assertSame(request.token(), request.token());
        test.assertEquals(DOMAIN, request.token().getDomain());
        test.assertEquals(KEY, request.token().getKey());
        test.assertEquals(42L, request.token().getExpiry());
    }

    @Test
    public void testMissingTokenIsTheSameExpiredTokenOnEveryCall(TestContext test) {
        Request request = request(new JsonObject());

        Assert.assertSame(request.token(), request.token());
        test.assertEquals(0L, request.token().getExpiry());
        test.assertNotNull(request.token().getDomain());
    }

    @Test
    public void testEachRequestHasItsOwnToken(TestContext test) {
        Token token = new Token(DOMAIN).setKey(KEY).setExpiry(42);
        JsonObject data = new JsonObject().put(ID_TOKEN, Serializer.json(token));

        Request first = request(data.copy());
        Request second = request(data.copy());

        Assert.assertNotSame(first.token(), second.token());
        test.assertEquals(first.token().getKey(), second.token().getKey());
    }

    @Test
    public void testRequestsThatDoNotCacheStillParseTheToken(TestContext test) {
        Token token = new Token(DOMAIN).setKey(KEY).setExpiry(42);
        JsonObject data = new JsonObject().put(ID_TOKEN, Serializer.json(token));

        // the interface default: custom requests that do not extend the abstract request.
        Request request = new Request() {
            @Override
            public com.codingchili.core.listener.transport.Connection connection() {
                return null;
            }

            @Override
            public JsonObject data() {
                return data;
            }

            @Override
            public void write(Object object) {
            }

            @Override
            public int size() {
                return 0;
            }
        };

        test.assertEquals(KEY, request.token().getKey());
        Assert.assertNotSame(request.token(), request.token());
    }
}
