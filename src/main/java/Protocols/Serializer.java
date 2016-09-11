package Protocols;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

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
     * Deserializes a json-string into an object.
     *
     * @param data   json-encoded string.
     * @param format the class to instantiate.
     * @return an Object of the specified class.
     */
    public static Object unpack(String data, Class format) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(data, format);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserializes a JSON string into an object.
     *
     * @param json   String containing the object values.
     * @param format class to be populated with the key/value pair.
     * @return an unpacked object.
     */
    public static Object unpack(JsonObject json, Class format) {
        return unpack(json.encode(), format);
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
}