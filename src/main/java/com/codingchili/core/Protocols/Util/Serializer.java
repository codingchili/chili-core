package com.codingchili.core.Protocols.Util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Robin Duda
 *         serializes objects to JSON and back.
 */
public class Serializer {

    /**
     * Serializes an object as JSON.
     *
     * @param object containing simple types that allow JSON transform.
     * @return a JSON string representing the object.
     */
    public static String pack(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserializes a json-string into a typed object.
     *
     * @param data  json-encoded string.
     * @param clazz the class to instantiate.
     * @return an object specified by the type parameter.
     */
    public static <T> T unpack(String data, Class clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return (T) mapper.readValue(data, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserializes a json-string into a typed object.
     *
     * @param json  json object to be unpacked.
     * @param clazz the class to instantiate.
     * @return an object specified by the type parameter.
     */
    public static <T> T unpack(JsonObject json, Class clazz) {
        return (json == null) ? null : unpack(json.encode(), clazz);
    }


    /**
     * Converts an object into a json object.
     *
     * @param object object to be converted.
     * @return JsonObject
     */
    public static JsonObject json(Object object) {
        return new JsonObject(pack(object));
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

            while((len = gzip.read(buffer)) != -1){
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