package com.codingchili.core.protocol;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.*;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;

import static io.vertx.core.json.impl.JsonUtil.*;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * See io.vertx.core.json.jackson.DatabindCodec for all available de/serializers.
 * The serializers in vert.x are not public, they are copied into this class for reuse.
 */
public class VertxSerializerModules {

    /**
     * Registers vert.x de/serializers for the given object mapper.
     *
     * @param mapper the mapper to register vert.x type support for.
     * @return the given mapper after adding a module with extended type support.
     */
    public static ObjectMapper registerTypes(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule(VertxSerializerModules.class.getSimpleName());

        module.addSerializer(JsonObject.class, new JsonObjectSerializer());
        module.addSerializer(JsonArray.class, new JsonArraySerializer());

        module.addSerializer(Instant.class, new InstantSerializer());
        module.addDeserializer(Instant.class, new InstantDeserializer());
        module.addSerializer(byte[].class, new ByteArraySerializer());
        module.addDeserializer(byte[].class, new ByteArrayDeserializer());
        module.addSerializer(Buffer.class, new BufferSerializer());
        module.addDeserializer(Buffer.class, new BufferDeserializer());

        return mapper.registerModule(module);
    }

    public static class JsonObjectSerializer extends JsonSerializer<JsonObject> {
        @Override
        public void serialize(JsonObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getMap());
        }
    }

    public static class JsonArraySerializer extends JsonSerializer<JsonArray> {
        @Override
        public void serialize(JsonArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(value.getList());
        }
    }

    public static class InstantSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(ISO_INSTANT.format(value));
        }
    }

    public static class InstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String text = p.getText();
            try {
                return Instant.from(ISO_INSTANT.parse(text));
            } catch (DateTimeException e) {
                throw new InvalidFormatException(p, "Expected an ISO 8601 formatted date time", text, Instant.class);
            }
        }
    }

    public static class ByteArraySerializer extends JsonSerializer<byte[]> {
        @Override
        public void serialize(byte[] value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(BASE64_ENCODER.encodeToString(value));
        }
    }

    public static class ByteArrayDeserializer extends JsonDeserializer<byte[]> {
        @Override
        public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String text = p.getText();
            try {
                return BASE64_DECODER.decode(text);
            } catch (IllegalArgumentException e) {
                throw new InvalidFormatException(p, "Expected a base64 encoded byte array", text, Instant.class);
            }
        }
    }

    public static class BufferSerializer extends JsonSerializer<Buffer> {
        @Override
        public void serialize(Buffer value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(BASE64_ENCODER.encodeToString(value.getBytes()));
        }
    }

    public static class BufferDeserializer extends JsonDeserializer<Buffer> {
        @Override
        public Buffer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String text = p.getText();
            try {
                return Buffer.buffer(BASE64_DECODER.decode(text));
            } catch (IllegalArgumentException e) {
                throw new InvalidFormatException(p, "Expected a base64 encoded byte array", text, Instant.class);
            }
        }
    }
}
