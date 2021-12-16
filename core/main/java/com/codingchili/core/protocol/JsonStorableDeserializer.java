package com.codingchili.core.protocol;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
    public JsonStorable deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        return new JsonStorable(parser.getCodec().readValue(parser, types));
    }
}
