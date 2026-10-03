package com.codingchili.core.protocol;

import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Map;

import com.codingchili.core.storage.JsonStorable;

/**
 * Provides support for deserializing into JsonStorable.
 */
public class JsonStorableDeserializer extends StdDeserializer<JsonStorable> {
    private final TypeReference<Map<String, Object>> types = new TypeReference<>() {
    };

    public JsonStorableDeserializer() {
        this(null);
    }

    public JsonStorableDeserializer(final Class<?> type) {
        super(type);
    }

    @Override
    public JsonStorable deserialize(final JsonParser parser, final DeserializationContext context) {
        return new JsonStorable(context.readValue(parser, types));
    }
}
