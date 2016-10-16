package com.codingchili.core.Protocols.util;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
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
public class SerializerTest {
    private Token token;

    @Before
    public void setUp() {
        TokenFactory factory = new TokenFactory("secret".getBytes());
        token = new Token(factory, "test");
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

        context.assertEquals("test", token.getDomain());
    }

    @Test
    public void testUnpackJsonObject(TestContext context) {
        String packed = Serializer.pack(token);
        Token token = Serializer.unpack(new JsonObject(packed), Token.class);

        context.assertEquals("test", token.getDomain());
    }

    @Test
    public void testObjectToJson(TestContext context) {
        JsonObject json = Serializer.json(token);

        context.assertEquals("test", json.getString(Strings.ID_DOMAIN));
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
