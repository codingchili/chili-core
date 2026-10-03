package com.codingchili.core.protocol;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.module.blackbird.BlackbirdModule;

import java.time.DateTimeException;
import java.time.Instant;

import static io.vertx.core.json.impl.JsonUtil.BASE64_DECODER;
import static io.vertx.core.json.impl.JsonUtil.BASE64_ENCODER;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * See io.vertx.core.json.jackson.DatabindCodec for all available de/serializers.
 * The serializers in vert.x are not public, they are copied into this class for reuse.
 */
public class VertxSerializerModules {

    /**
     * Registers vert.x de/serializers for the given object mapper.
     *
     * @param builder the mapper to register vert.x type support for.
     * @return the given mapper after adding a module with extended type support.
     */
    public static <T extends ObjectMapper, V extends MapperBuilder<T, V>> MapperBuilder<T, V> registerTypes(MapperBuilder<T,V> builder) {
        builder.addModule(new JsonTypesModule());
        builder.addModule(new BlackbirdModule());
        builder.addModule(VertxJsonTypes());
        return builder;
    }

    public static JacksonModule VertxJsonTypes() {
        SimpleModule module = new SimpleModule(VertxSerializerModules.class.getSimpleName());

        module.addSerializer(JsonObject.class, new JsonObjectSerializer());
        module.addSerializer(JsonArray.class, new JsonArraySerializer());

        module.addSerializer(Instant.class, new InstantSerializer());
        module.addDeserializer(Instant.class, new InstantDeserializer());
        module.addSerializer(byte[].class, new ByteArraySerializer());
        module.addDeserializer(byte[].class, new ByteArrayDeserializer());
        module.addSerializer(Buffer.class, new BufferSerializer());
        module.addDeserializer(Buffer.class, new BufferDeserializer());

        return module;
    }

    public static class JsonObjectSerializer extends ValueSerializer<JsonObject> {
        @Override
        public void serialize(JsonObject value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writePOJO(value.getMap());
        }
    }

    public static class JsonArraySerializer extends ValueSerializer<JsonArray> {
        @Override
        public void serialize(JsonArray value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writePOJO(value.getList());
        }
    }

    public static class InstantSerializer extends ValueSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeString(ISO_INSTANT.format(value));
        }
    }

    public static class InstantDeserializer extends ValueDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) {
            String text;
            try {
                text = p.getString();
                return Instant.from(ISO_INSTANT.parse(text));
            } catch (DateTimeException e) {
                throw new RuntimeException("Expected an ISO 8601 formatted date time", e);
            }
        }
    }

    public static class ByteArraySerializer extends ValueSerializer<byte[]> {
        @Override
        public void serialize(byte[] value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeString(BASE64_ENCODER.encodeToString(value));
        }
    }

    public static class ByteArrayDeserializer extends ValueDeserializer<byte[]> {
        @Override
        public byte[] deserialize(JsonParser p, DeserializationContext ctxt) {
            try {
                String text = p.getString();
                return BASE64_DECODER.decode(text);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Expected a base64 encoded byte array", e);
            }
        }
    }

    public static class BufferSerializer extends ValueSerializer<Buffer> {
        @Override
        public void serialize(Buffer value, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeString(BASE64_ENCODER.encodeToString(value.getBytes()));
        }
    }

    public static class BufferDeserializer extends ValueDeserializer<Buffer> {
        @Override
        public Buffer deserialize(JsonParser p, DeserializationContext ctxt) {
            try {
                String text = p.getText();
                return Buffer.buffer(BASE64_DECODER.decode(text));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Expected a base64 encoded byte array", e);
            }
        }
    }
}
