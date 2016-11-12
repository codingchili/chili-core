package com.codingchili.core.Protocol;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Security.Token;
import com.codingchili.core.Security.TokenFactory;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class SerializerTest {
    private static final String TEST = "test";
    private static final String SECRET = "secret";
    private Token token;

    @Before
    public void setUp() {
        TokenFactory factory = new TokenFactory(SECRET.getBytes());
        token = new Token(factory, TEST);
    }

    @Test
    public void testPackObjectToJsonString(TestContext context) {
        String packed = Serializer.pack(token);

        context.assertTrue(packed.startsWith("{"));
        context.assertTrue(packed.endsWith("}"));
    }

    @Test
    public void testUnpackString(TestContext context) {
        String packed = Serializer.pack(token);
        token = Serializer.unpack(packed, Token.class);

        context.assertEquals(TEST, token.getDomain());
    }

    @Test
    public void testUnpackJsonObject(TestContext context) {
        String packed = Serializer.pack(token);
        Token token = Serializer.unpack(new JsonObject(packed), Token.class);

        context.assertEquals(TEST, token.getDomain());
    }

    @Test
    public void testObjectToJson(TestContext context) {
        JsonObject json = Serializer.json(token);

        context.assertEquals(TEST, json.getString(Strings.ID_DOMAIN));
    }

    @Test
    public void testGzipBytesAndUnzip(TestContext context) {
        String text = "string";

        byte[] gzip = Serializer.gzip(text.getBytes());
        context.assertNotEquals(new String(text.getBytes()), new String(gzip));

        byte[] ungzip = Serializer.ungzip(gzip);
        context.assertEquals(new String(text.getBytes()), new String(ungzip));
    }
}
