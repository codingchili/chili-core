package com.codingchili.core.protocol;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.storage.JsonStorable;

/**
 * Jackson module that configures deserializers for JsonObject and JsonStorable.
 */
public class JsonTypesModule extends SimpleModule {
    {
        addDeserializer(JsonObject.class, new JsonObjectDeserializer());
        addDeserializer(JsonStorable.class, new JsonStorableDeserializer());
    }
}
