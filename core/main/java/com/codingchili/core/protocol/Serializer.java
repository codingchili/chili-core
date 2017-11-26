package com.codingchili.core.protocol;


import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.exception.SerializerPayloadException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.codingchili.core.configuration.CoreStrings.ID_COLLECTION;
import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;

/**
 * @author Robin Duda
 * serializes objects to JSON and back.
 */
public class Serializer {
    // use vertx's objectmapper, it comes with custom serializer modules.
    private static ObjectMapper mapper = Json.mapper;

    static {
        Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Json.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Json.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Json.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    /**
     * Serializes an object as JSON.
     *
     * @param object containing JSON transformable types.
     * @return a JSON string representing the object.
     */
    public static String pack(Object object) {
        if (object instanceof JsonObject) {
            return ((JsonObject) object).encodePrettily();
        } else {
            try {
                return mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new CoreRuntimeException(e.getMessage());
            }
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
        } else {
            return Json.encodeToBuffer(object);
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
            return mapper.readValue(data, clazz);
        } catch (IOException e) {
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
                return json.mapTo(clazz);
            }
        }
    }

    /**
     * Converts an object into a json object.
     *
     * @param object object to be converted.
     * @return JsonObject
     */
    public static JsonObject json(Object object) {
        if (object instanceof JsonObject) {
            return (JsonObject) object;
        } else if (object instanceof Collection) {
            JsonArray array = new JsonArray();
            ((Collection<?>) object).stream()
                    .map(Serializer::json)
                    .forEach(array::add);
            return new JsonObject().put(ID_COLLECTION, array);
        } else {
            return JsonObject.mapFrom(object);
        }
    }

    /**
     * Extract the value at the given path from the given jsonobject.
     * The path is formatted by delimiting fields with a dot.
     *
     * @param json the json object to extract a value from
     * @param path the path of the value, for example "person.name.last", last
     *             may be an array or regular field. when extracting values
     *             from arrays only plain fields are allowed, no objects in arrays.
     * @param <T>  free class cast.
     * @return the extracted value type casted to the type parameter.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] getValueByPath(JsonObject json, String path) {
        String[] fields = path.replace(STORAGE_ARRAY, "").split("\\.");
        String targetField = fields[fields.length - 1];

        for (int i = 0; i < fields.length - 1; i++) {
            json = json.getJsonObject(fields[i]);
        }

        Object targetValue = json.getValue(targetField);
        if (targetValue instanceof JsonArray) {
            Comparable[] objects = new Comparable[((JsonArray) targetValue).size()];

            for (int i = 0; i < objects.length; i++) {
                objects[i] = (Comparable) ((JsonArray) targetValue).getValue(i);
            }
            return (T[]) objects;
        } else {
            return (T[]) new Comparable[]{(Comparable) targetValue};
        }
    }

    /**
     * Compresses a byte array using gzip.
     *
     * @param data data to be compressed.
     * @return data compressed with gzip.
     */
    public static byte[] gzip(byte[] data) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(output);
            gzip.write(data);
            gzip.close();
            output.close();
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
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int len;

            while ((len = gzip.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            out.close();
            gzip.close();

            return out.toByteArray();
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
        Map<String, String> model = new HashMap<>();
        String className = template.getName();
        if (cache.containsKey(className)) {
            model = cache.get(className);
        } else {
            for (Field field : template.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    String generic = field.getGenericType().getTypeName();
                    model.put(field.getName(), generic);
                }
            }
        }
        cache.put(className, model);
        return model;
    }

    private static Map<String, Map<String, String>> cache = new HashMap<>();
}