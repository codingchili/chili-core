package com.codingchili.core.protocol;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.testing.NestedObject;
import com.codingchili.core.testing.StorageObject;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Tests for the serializer.
 */
@RunWith(VertxUnitRunner.class)
public class SerializerTest {
    private static final String TEST = "test";
    private static final String SECRET = "secret";
    private static final String OWNER = "owner";
    private static final String PROPERTIES_OWNER = "properties.owner";
    private Token token;

    @Before
    public void setUp() {
        TokenFactory factory = new TokenFactory(SECRET.getBytes());
        token = new Token(factory, TEST);
        token = token.addProperty(OWNER, getClass().getSimpleName());
    }

    @Test
    public void testPackObjectToJsonString(TestContext test) {
        String packed = Serializer.pack(token);

        test.assertTrue(packed.startsWith("{"));
        test.assertTrue(packed.endsWith("}"));
    }

    @Test
    public void testUnpackString(TestContext test) {
        String packed = Serializer.pack(token);
        token = Serializer.unpack(packed.getBytes(), Token.class);

        test.assertEquals(TEST, token.getDomain());
    }

    @Test
    public void testUnpackJsonObject(TestContext test) {
        String packed = Serializer.pack(token);
        Token token = Serializer.unpack(new JsonObject(packed), Token.class);

        test.assertEquals(TEST, token.getDomain());
    }

    @Test
    public void testObjectToJson(TestContext context) {
        JsonObject json = Serializer.json(token);

        context.assertEquals(TEST, json.getString(CoreStrings.ID_DOMAIN));
    }

    @Test
    public void testGzipBytesAndUnzip(TestContext test) {
        String text = "string";

        byte[] gzip = Serializer.gzip(text.getBytes());
        test.assertNotEquals(new String(text.getBytes()), new String(gzip));

        byte[] ungzip = Serializer.ungzip(gzip);
        test.assertEquals(new String(text.getBytes()), new String(ungzip));
    }

    @Test
    public void testDescribeClass(TestContext test) {
        Map<String, String> model = Serializer.describe(StorageObject.class);
        test.assertTrue(model.containsKey("keywords"));
        test.assertTrue(model.containsKey("level"));
        test.assertTrue(model.containsKey("name"));
        test.assertTrue(model.containsKey("nested"));
        test.assertEquals(Integer.class.getName(), model.get("level"));
        test.assertEquals("java.util.List<java.lang.String>", model.get("keywords"));
        test.assertEquals(4, model.values().size());
    }

    @Test
    public void testGetJsonValueByPath(TestContext test) {
        Optional<String> value = Serializer.<String>getValueByPath(Serializer.json(token), PROPERTIES_OWNER).stream().findFirst();
        test.assertTrue(value.isPresent());
        test.assertEquals(getClass().getSimpleName(), value.get());
    }

    @Test
    public void testGetObjectValueByPath(TestContext test) {
        Optional<String> value = Serializer.<String>getValueByPath(token, PROPERTIES_OWNER).stream().findFirst();
        test.assertTrue(value.isPresent());
        test.assertEquals(getClass().getSimpleName(), value.get());
    }

    @Test
    public void testSerializeYaml(TestContext test) {
        StorageObject storable = new StorageObject();
        NestedObject nested = new NestedObject().setName("anjah");
        List<Integer> numbers = Collections.singletonList(1);
        nested.setNumbers(numbers);
        storable.setLevel(2).setName("robin").setNested(nested);

        String yaml = Serializer.yaml(storable);
        StorageObject yamlStorable = Serializer.unyaml(yaml.getBytes(), StorageObject.class);

        test.assertEquals(Serializer.pack(storable), Serializer.pack(yamlStorable));
    }
}
