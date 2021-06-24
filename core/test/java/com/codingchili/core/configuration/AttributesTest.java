package com.codingchili.core.configuration;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Optional;

import com.codingchili.core.protocol.Serializer;

/**
 * Tests the implementation of attributes.
 */
@RunWith(VertxUnitRunner.class)
public class AttributesTest {
    private static final String KEY = "key";
    private Attributes attributes = new Attributes() {
    };

    @Before
    public void setUp() {
        this.attributes = new Attributes() {
        };
    }

    @Test
    public void testSetAttributes() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KEY, "");
        map.put(KEY, 0L);
        map.put(KEY, 0d);
        map.put(KEY, true);

        attributes.setAttributes(map);

        Assert.assertNotNull(attributes.getString(KEY));
        Assert.assertNotNull(attributes.getInt(KEY));
        Assert.assertNotNull(attributes.getDouble(KEY));
        Assert.assertNotNull(attributes.getBool(KEY));
    }

    @Test
    public void testClearAttributes() {
        attributes.put(KEY, 1);
        attributes.put(KEY, "");
        attributes.put(KEY, 0L);
        attributes.put(KEY, false);
        attributes.put(KEY, 0.5d);

        attributes.clear();

        Assert.assertFalse(attributes.getDouble(KEY).isPresent());
        Assert.assertFalse(attributes.getString(KEY).isPresent());
        Assert.assertFalse(attributes.getBool(KEY).isPresent());
        Assert.assertFalse(attributes.getInt(KEY).isPresent());
        Assert.assertEquals(0, attributes.size());
    }

    @Test
    public void testAddIntegers(TestContext test) {
        attributes.put(KEY, 1);
        Optional<Integer> integer = attributes.getInt(KEY);

        Assert.assertTrue(integer.isPresent());
        test.assertTrue(integer.get().equals(1));
    }

    @Test
    public void testAddObject() {
        attributes.put(KEY, 0L);
        Optional<Object> object = attributes.getObject(KEY);
        Assert.assertTrue(object.isPresent());
        Assert.assertTrue(object.get().equals(0L));
    }

    @Test
    public void testTypesAreStrict() {
        attributes.put(KEY, 0L);
        Assert.assertFalse(attributes.getBool(KEY).isPresent());
        Assert.assertFalse(attributes.getDouble(KEY).isPresent());
        Assert.assertFalse(attributes.getString(KEY).isPresent());
        Assert.assertFalse(attributes.getInt(KEY).isPresent());
    }

    @Test
    public void testAddDoubles(TestContext test) {
        attributes.put(KEY, 1d);
        Optional<Double> adoub = attributes.getDouble(KEY);

        Assert.assertTrue(adoub.isPresent());
        test.assertTrue(adoub.get().equals(1d));
    }

    @Test
    public void testAddStrings(TestContext test) {
        attributes.put(KEY, "s");
        Optional<String> string = attributes.getString(KEY);

        Assert.assertTrue(string.isPresent());
        test.assertTrue(string.get().equals("s"));
    }

    @Test
    public void testAddBooleans(TestContext test) {
        attributes.put(KEY, true);
        Optional<Boolean> bool = attributes.getBool(KEY);

        Assert.assertTrue(bool.isPresent());
        test.assertTrue(bool.get().equals(true));
    }

    @Test
    public void testSerializeAttributes(TestContext test) {
        attributes.put("K1", "str");
        attributes.put("K2", 0);
        attributes.put("K3", 0d);
        attributes.put("K4", true);

        Attributes attr = Serializer.unpack(Serializer.json(attributes), AttributeImpl.class);
        test.assertTrue(attr.size() == 4);
    }
}
