package com.codingchili.core.storage;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * Tests for the storable wrapper of the JsonObject.
 */
@RunWith(VertxUnitRunner.class)
public class JsonStorableTest {

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
