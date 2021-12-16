package com.codingchili.core.protocol;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.Pool;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.*;
import io.vertx.core.json.jackson.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.exception.SerializerPayloadException;
import com.codingchili.core.storage.JsonStorable;
import com.codingchili.core.storage.Storable;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Serializes objects to JSON or YAML and back. Utility methods for gzip and class definition generation.
 */
public class Serializer {
    // use vertx's objectmapper, it comes with custom serializer modules.
    public static ObjectMapper json = DatabindCodec.mapper();
    public static ObjectMapper yaml = new ObjectMapper(new YAMLFactory()
            .configure(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE, true)
    );

    static {
        // enable pretty printing for all json.
        json.configure(SerializationFeature.INDENT_OUTPUT, true);
        json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        json.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        json.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        json.configure(SerializationFeature.INDENT_OUTPUT, true);

        // this configuration method is deprecated; vertx doesn't internally use the new builder pattern
        // for the mapper which means that the upgraded way is not accessible here.
        // In 3.x this behavior will be the default so this could then be removed
        // regardless if the builder is made available from vertx or not.
        // this configures a default polymorphic type validator (3.10), which will deny
        // deserialization into weakly typed (and known dangerous) field types such as 'object' etc.
        json.configure(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES, true);

        yaml.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        yaml.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        yaml.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // vertx does not provide a yaml mapper, configure with the same serializers.
        yaml = VertxSerializerModules.registerTypes(yaml);
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
        T object = kryo.apply(instance);
        pool.free(instance);
        return object;
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
            return ((JsonObject) object).encodePrettily();
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
            if (object instanceof JsonObject) {
                JsonNode node = new ObjectMapper().readTree(((JsonObject) object).encode());
                return yaml.writeValueAsString(node);
            } else {
                return yaml.writeValueAsString(object);
            }
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
    public static <T> Collection<T> getValueByPath(JsonObject json, String path) {
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
            return Arrays.asList((T[]) objects);
        } else {
            return Collections.singleton((T) targetValue);
        }
    }

    // stores fields that has been set as accessible for the given class.
    private static final Map<Class<?>, Map<String, Field>> reflectCache = new HashMap<>();

    /**
     * Gets a value by the given path for an object.
     *
     * @param object the object to get the path value of.
     * @param path   the path to the field to retrieve the value of, may be an object or collection.
     * @param <T>    the type of the field to retrieve.
     * @return a list of values matching the path.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> getValueByPath(Object object, String path) {
        if (object instanceof JsonObject) {
            return getValueByPath((JsonObject) object, path);
        } else {
            String[] fields = path.replace(STORAGE_ARRAY, "").split("\\.");

            for (String fieldName : fields) {
                if (object == null) {
                    throw new CoreRuntimeException(CoreStrings.getValueByPathContainsNull(fieldName, fields));
                }

                if (object instanceof Storable && fieldName.equals(Storable.idField)) {
                    return Collections.singleton((T) ((Storable) object).getId());
                } else if (object instanceof Map) {
                    object = ((Map) object).get(fieldName);
                } else if (object instanceof JsonObject) {
                    object = ((JsonObject) object).getValue(fieldName);
                } else {
                    try {
                        Class<?> origin = object.getClass();
                        boolean found = false;

                        for (Class<?> iterator = origin; iterator != Object.class; iterator = iterator.getSuperclass()) {
                            Map<String, Field> fieldSet = reflectCache.computeIfAbsent(iterator, (k) -> new TreeMap<>());
                            Field field = fieldSet.get(fieldName);

                            if (field == null) {
                                for (Field member : iterator.getDeclaredFields()) {
                                    if (member.getName().equals(fieldName)) {
                                        member.setAccessible(true);
                                        fieldSet.put(fieldName, field);
                                        field = member;
                                        found = true;
                                    }
                                }
                            }
                            if (field != null) {
                                object = field.get(object);
                            }
                        }
                        if (!found) {
                            throw new CoreRuntimeException(String.format("Not able to find field '%s' from class '%s'.",
                                    fieldName, origin.getName()));

                        }
                    } catch (Throwable e) {
                        throw new CoreRuntimeException(CoreStrings.getReflectionErrorInSerializer(path));
                    }
                }
            }
            if (object instanceof Collection) {
                return (Collection<T>) object;
            } else {
                return Collections.singleton((T) object);
            }
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
                if ((field.getModifiers() & Modifier.STATIC) == 0 && !field.isSynthetic()) {
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