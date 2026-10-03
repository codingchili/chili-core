package com.codingchili.core.protocol;

import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.Map;

/**
 * Provides support for deserializing into JsonObject.
 */
public class JsonObjectDeserializer extends StdDeserializer<JsonObject> {
    private final TypeReference<Map<String, Object>> types = new TypeReference<>() {
    };

    public JsonObjectDeserializer() {
        this(null);
    }

    public JsonObjectDeserializer(final Class<?> type) {
        super(type);
    }

    @Override
    public JsonObject deserialize(final JsonParser parser, final DeserializationContext context) {
        return new JsonObject(context.readValue(parser, types));
    }
}
