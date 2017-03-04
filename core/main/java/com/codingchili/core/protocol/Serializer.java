package com.codingchili.core.protocol;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;

/**
 * @author Robin Duda
 *         serializes objects to JSON and back.
 */
public class Serializer {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Serializes an object as JSON.
     *
     * @param object containing JSON transformable types.
     * @return a JSON string representing the object.
     */
    public static String pack(Object object) {
        try {
            if (object instanceof JsonObject) {
                return ((JsonObject) object).encode();
            } else {
                return mapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Dematerializes a json-string into a typed object.
     *
     * @param data  json-encoded string.
     * @param clazz the class to instantiate.
     * @return an object specified by the type parameter.
     */
    @SuppressWarnings("unchecked")
    public static <T> T unpack(String data, Class clazz) {
        try {
            return (T) mapper.readValue(data, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Dematerializes a json-string into a typed object.
     *
     * @param json  json object to be unpacked.
     * @param clazz the class to instantiate.
     * @return an object specified by the type parameter.
     */
    @SuppressWarnings("unchecked")
    public static <T> T unpack(JsonObject json, Class clazz) {
        if (clazz.isInstance(json)) {
            return (T) json;
        } else {
            return (json == null) ? null : unpack(json.encode(), clazz);
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
        } else {
            return new JsonObject(pack(object));
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
}