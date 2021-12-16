package com.codingchili.core.storage;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import com.codingchili.core.protocol.Serializer;

/**
 * Tests for the storable wrapper of the JsonObject.
 */
@RunWith(VertxUnitRunner.class)
public class JsonStorableTest {
    private final JsonObject json = new JsonObject()
            .put("snorkels", new JsonArray()
                    .add("foo")
                    .add("snoo"))
            .put(Storable.idField, "000")
            .put("object", new JsonObject().put("prop", "prop1"))
            .put("key", "value");

    @Test
    public void deserializeIntoJsonObject() {
        var object = Serializer.unpack(json.encode(), JsonObject.class);
        Assert.assertEquals(json.fieldNames().size(), object.fieldNames().size());
    }

    @Test
    public void deserializeIntoJsonStorable() {
        var storable = Serializer.unpack(json.encode(), JsonStorable.class);
        Assert.assertNotNull(storable.getId());
        Assert.assertEquals(json.fieldNames().size(), storable.fieldNames().size());
    }

    @Test
    public void instantiateEmptyHasId() {
        Assert.assertFalse(new JsonStorable().getId().isEmpty());
    }

    @Test
    public void instantiateWithId() {
        var id = UUID.randomUUID().toString();
        var storable = new JsonStorable(id);
        Assert.assertEquals(id, storable.getId());
    }

    @Test
    public void instantiateExistingWithoutKey() {
        var json = new JsonObject();
        var storable = new JsonStorable(json);
        Assert.assertFalse(storable.getId().isEmpty());
    }

    @Test
    public void instantiateExistingRetainsKeys() {
        var id = UUID.randomUUID().toString();
        var json = new JsonObject()
                .put(Storable.idField, id);

        var storable = new JsonStorable(json);
        Assert.assertEquals(id, storable.getId());
    }

    @Test
    public void copyNewChangesId() {
        var id = UUID.randomUUID().toString();
        var storable = new JsonStorable(id);
        Assert.assertNotEquals(id, storable.copyNew().getId());
    }

    @Test
    public void copyDoesNotChangeId() {
        var id = UUID.randomUUID().toString();
        var storable = new JsonStorable(id);
        Assert.assertEquals(id, storable.getId());
    }
}
