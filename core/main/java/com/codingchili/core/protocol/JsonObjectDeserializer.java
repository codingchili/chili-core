package com.codingchili.core.protocol;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
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
    public JsonObject deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        return new JsonObject(parser.getCodec().readValue(parser, types));
    }
}
