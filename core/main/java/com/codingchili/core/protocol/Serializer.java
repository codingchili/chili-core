package com.codingchili.core.protocol;


import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.exception.SerializerPayloadException;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.Pool;
import io.vertx.core.buffer.Buffer;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;
import tools.jackson.module.blackbird.BlackbirdModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.codingchili.core.configuration.CoreStrings.ID_COLLECTION;

/**
 * Serializes objects to JSON or YAML and back. Utility methods for gzip and class definition generation.
 */
public class Serializer {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    public static ObjectMapper json = createJsonMapper();
    public static ObjectMapper yaml = createYAMLMapper();

    public static JsonMapper createJsonMapper() {
        var json = JsonMapper.builder()
                // output is compact by default, see SystemSettings#setPrettyEncoding to indent it.
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS);

        return VertxSerializerModules.registerTypes(configure(json)).build();
    }

    public static YAMLMapper createYAMLMapper() {
        var yaml = YAMLMapper.builder()
                .configure(YAMLWriteFeature.LITERAL_BLOCK_STYLE, true);

        return VertxSerializerModules.registerTypes(configure(yaml)).build();
    }

    private static <T extends ObjectMapper, B extends MapperBuilder<T, B>> B configure(B builder) {
        return builder
                .changeDefaultPropertyInclusion(include -> include.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private static final Pool<Kryo> pool = new Pool<Kryo>(true, true, 128) {
        protected Kryo create() {
            Kryo kryo = new Kryo();
            // this instance should not be used for de-serializing arbitrary classes.
            kryo.setRegistrationRequired(false);
            return kryo;
        }
    };

    /**
     * Execute with a pooled kryo instance.
     *
     * @param kryo a pooled kryo instance.
     * @param <T>  the type of value that is returned by the given kryo operation.
     * @return the value that is returned by the kryo invocation.
     */
    public static <T> T kryo(Function<Kryo, T> kryo) {
        Kryo instance = pool.obtain();
        try {
            return kryo.apply(instance);
        } finally {
            pool.free(instance);
        }
    }

    /**
     * Configures the current kryo instance to skip copying and serializing of transient fields.
     * If the serializer configuration is already up to date then no changes will be committed.
     *
     * @param kryo     the kryo instance to apply the configuration to.
     * @param theClass the class for which the serializer configuration is to be changed.
     */
    public static void skipTransient(Kryo kryo, Class theClass) {
        FieldSerializer serializer = (FieldSerializer) kryo.getSerializer(theClass);
        FieldSerializer.FieldSerializerConfig config = serializer.getFieldSerializerConfig();

        if (config.getCopyTransient()) {
            // avoid calling updateFields if transient fields are already disabled.
            config.setCopyTransient(false);
            config.setSerializeTransient(false);
            serializer.updateFields();
        }
    }

    /**
     * Serializes an object as JSON.
     *
     * @param object containing JSON transformable types.
     * @return a JSON string representing the object.
     */
    public static String pack(Object object) {
        if (object instanceof JsonObject) {
            if (json.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
                return ((JsonObject) object).encodePrettily();
            } else {
                return ((JsonObject) object).encode();
            }
        } else {
            try {
                return json.writeValueAsString(object);
            } catch (Throwable e) {
                throw new CoreRuntimeException(e.getMessage());
            }
        }
    }

    /**
     * Serializes an object as YAML.
     *
     * @param object the object to serialize
     * @return a YAML string representing the object.
     */
    public static String yaml(Object object) {
        try {
            // JsonObject is supported by the registered vert.x type serializers.
            return yaml.writeValueAsString(object);
        } catch (Throwable e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    /**
     * Converts any object into a buffer in json format.
     *
     * @param object the object to serialize.
     * @return a Buffer of the json encoded object.
     */
    public static Buffer buffer(Object object) {
        if (object instanceof JsonObject) {
            return ((JsonObject) object).toBuffer();
        } else if (object instanceof Buffer) {
            return (Buffer) object;
        } else {
            return Buffer.buffer(json.writeValueAsBytes(object));
        }
    }

    /**
     * Dematerializes a json-string into a typed object.
     *
     * @param data  json-encoded string.
     * @param clazz the class to instantiate.
     * @param <T>   must be bound to the clazz parameter
     * @return an object specified by the type parameter.
     */
    public static <T> T unpack(String data, Class<T> clazz) {
        try {
            return json.readValue(data, clazz);
        } catch (Throwable e) {
            throw new SerializerPayloadException(e.getMessage(), clazz);
        }
    }

    /**
     * Dematerializes a yaml-string into a typed object.
     *
     * @param data  the yaml-encoded string.
     * @param clazz the class to instantiate.
     * @param <T>   must be bound to the class parameter.
     * @return an object specified by the type parameters.
     */
    public static <T> T unyaml(String data, Class<T> clazz) {
        try {
            return yaml.readValue(data, clazz);
        } catch (Throwable e) {
            throw new SerializerPayloadException(e.getMessage(), clazz);
        }
    }

    /**
     * Dematerializes a json-string into a typed object.
     *
     * @param json  json object to be unpacked.
     * @param clazz the class to instantiate.
     * @param <T>   must be bound to the clazz parameter
     * @return an object specified by the type parameter.
     */
    @SuppressWarnings("unchecked")
    public static <T> T unpack(JsonObject json, Class<T> clazz) {
        if (clazz.isInstance(json)) {
            return (T) json;
        } else {
            if (json == null) {
                throw new SerializerPayloadException("null", clazz);
            } else {
                try {
                    return Serializer.json.convertValue(json, clazz);
                } catch (Throwable e) {
                    throw new SerializerPayloadException(e.getMessage(), clazz);
                }
            }
        }
    }

    /**
     * Converts an object into a json object. A buffer is read as json text, a collection
     * is wrapped in an object with the items as an array.
     *
     * @param object object to be converted.
     * @return JsonObject
     */
    public static JsonObject json(Object object) {
        if (object instanceof JsonObject) {
            return (JsonObject) object;
        } else if (object instanceof Buffer) {
            return ((Buffer) object).toJsonObject();
        } else if (object instanceof Collection) {
            JsonArray array = new JsonArray();
            ((Collection<?>) object).stream()
                    .map(Serializer::json)
                    .forEach(array::add);
            return new JsonObject().put(ID_COLLECTION, array);
        } else {
            return new JsonObject(json.convertValue(object, MAP_TYPE));
        }
    }

    /**
     * Compresses a byte array using gzip.
     *
     * @param data data to be compressed.
     * @return data compressed with gzip.
     */
    public static byte[] gzip(byte[] data) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(data);
            gzip.finish();
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Decompress a byte array using gzip.
     *
     * @param data to be decompressed.
     * @return data decompressed with gzip.
     */
    public static byte[] ungzip(byte[] data) {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return gzip.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Serializes the non static member fields of the given class.
     *
     * @param template the class of which members should be described.
     * @return a map that can be serialized to json with field name
     * mapped to type.
     */
    public static Map<String, String> describe(Class<?> template) {
        return cache.computeIfAbsent(template.getName(), className -> {
            Map<String, String> model = new HashMap<>();
            for (Field field : template.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.STATIC) == 0 && !field.isSynthetic()) {
                    model.put(field.getName(), field.getGenericType().getTypeName());
                }
            }
            return model;
        });
    }

    private static final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();
}