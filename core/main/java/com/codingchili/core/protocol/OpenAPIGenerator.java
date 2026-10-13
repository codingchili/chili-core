package com.codingchili.core.protocol;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.*;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * Generates OpenAPI 3 definitions from a {@link Protocol}, without dependencies. The definition is read from the
 * routes of the protocol: the description, the roles, and for typed routes the types of the input and the output,
 * see {@link TypedRoute}. Validate a result with https://apidevtools.org/swagger-parser/online/
 * <ul>
 * <li>Each route is a {@code POST} to {@code /<route>}, the target of the protocol is the tag of the routes.</li>
 * <li>The input is the request body, a route without input has no request body. Routes that are not typed are
 * documented without a request body unless a {@link DataModel} is set, which is the input.</li>
 * <li>Responses are answered with the status in the body, the HTTP status is always 200. The response is the
 * output of the route or {@code Error}, which contains the {@link ResponseStatus}.</li>
 * <li>Types become schemas in the components: records, and classes with fields and getters, nested and in
 * collections, maps and optionals. Recursive types are referenced.</li>
 * </ul>
 */
public class OpenAPIGenerator {
    private static final String COMPONENTS = "#/components/schemas/";
    private static final String ERROR = "Error";
    private static final String DEFAULT_TITLE = "chili-core protocol";
    private static final String DEFAULT_VERSION = "1.0.0";

    private final JsonObject schemas = new JsonObject();
    private final Map<String, Class<?>> names = new HashMap<>();

    private OpenAPIGenerator() {
    }

    /**
     * @param protocol the protocol instance to generate an API definition from.
     * @return a YAML OpenAPI v3 definition generated from a protocol.
     * @see #toJson(Protocol)
     */
    public static String toYaml(Protocol<?> protocol) {
        return Serializer.yaml(toObject(protocol));
    }

    /**
     * @param protocol the protocol instance to generate an API definition from.
     * @return a JSON OpenAPI v3 definition generated from a protocol.
     */
    public static String toJson(Protocol<?> protocol) {
        return toObject(protocol).encodePrettily();
    }

    /**
     * @param protocol the protocol instance to generate an API definition from.
     * @return an OpenAPI v3 definition generated from a protocol.
     */
    public static JsonObject toObject(Protocol<?> protocol) {
        return toObject(protocol, null, DEFAULT_VERSION);
    }

    /**
     * @param protocol the protocol instance to generate an API definition from.
     * @param title    the title of the API, defaults to the target of the protocol.
     * @param version  the version of the API.
     * @return an OpenAPI v3 definition generated from a protocol.
     */
    public static JsonObject toObject(Protocol<?> protocol, String title, String version) {
        return new OpenAPIGenerator().generate(protocol.getSchema(), title, version);
    }

    /**
     * @param type a type, possibly generic.
     * @return the class of the type, null if it has none.
     */
    static Class<?> rawType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType parameterized) {
            return rawType(parameterized.getRawType());
        } else if (type instanceof GenericArrayType array) {
            return Array.newInstance(rawType(array.getGenericComponentType()), 0).getClass();
        }
        return null;
    }

    private JsonObject generate(ProtocolDescription<?> description, String title, String version) {
        String target = (description.getTarget() == null) ? "" : description.getTarget();
        String tag = (target.isEmpty()) ? "default" : target;
        JsonObject paths = new JsonObject();

        description.getRoutes().values().stream()
                .sorted(Comparator.comparing(Route::getRoute))
                .forEach(route -> paths.put("/" + route.getRoute(), new JsonObject().put("post", operation(route, tag))));

        schemas.put(ERROR, error());

        return new JsonObject()
                .put("openapi", "3.0.0")
                .put("info", new JsonObject()
                        .put("title", (title != null) ? title : (target.isEmpty() ? DEFAULT_TITLE : target))
                        .put("description", String.valueOf(description.getDescription()))
                        .put("version", version))
                .put("tags", new JsonArray().add(new JsonObject().put("name", tag)))
                .put("paths", paths)
                .put("components", new JsonObject().put("schemas", schemas));
    }

    private JsonObject operation(Route<?> route, String tag) {
        JsonObject operation = new JsonObject()
                .put("tags", new JsonArray().add(tag))
                .put("operationId", route.getRoute())
                .put("summary", route.getDescription());

        if (route.getRoles() != null) {
            JsonArray roles = new JsonArray();
            Arrays.stream(route.getRoles()).forEach(role -> roles.add(role.getName()));
            operation.put("x-roles", roles);
        }

        if (route.getInputType() != null) {
            operation.put("requestBody", new JsonObject()
                    .put("required", true)
                    .put("content", json(schema(route.getInputType()))));
        }

        JsonObject ok = new JsonObject();
        JsonObject error = new JsonObject().put("$ref", COMPONENTS + ERROR);

        if (route.getResultType() != null && !isVoid(route.getResultType())) {
            ok.put("oneOf", new JsonArray().add(schema(route.getResultType())).add(error));
        } else {
            ok.put("$ref", COMPONENTS + ERROR);
        }
        operation.put("responses", new JsonObject().put("200", new JsonObject()
                .put("description", "The status is in the body, ACCEPTED when the route succeeds.")
                .put("content", json(ok))));
        return operation;
    }

    private JsonObject error() {
        JsonArray statuses = new JsonArray();
        Arrays.stream(ResponseStatus.values()).forEach(status -> statuses.add(status.name()));

        return new JsonObject()
                .put("type", "object")
                .put("properties", new JsonObject()
                        .put("status", new JsonObject().put("type", "string").put("enum", statuses))
                        .put("message", new JsonObject().put("type", "string"))
                        .put("route", new JsonObject().put("type", "string"))
                        .put("target", new JsonObject().put("type", "string")))
                .put("required", new JsonArray().add("status"));
    }

    private static JsonObject json(JsonObject schema) {
        return new JsonObject().put("application/json", new JsonObject().put("schema", schema));
    }

    private static boolean isVoid(Type type) {
        return type == void.class || type == Void.class;
    }

    /**
     * @return the schema of a type, or a reference to it in the components if it is an object.
     */
    private JsonObject schema(Type type) {
        Class<?> raw = rawType(type);
        Type[] arguments = (type instanceof ParameterizedType parameterized) ?
                parameterized.getActualTypeArguments() : new Type[0];

        if (raw == null) {
            return new JsonObject();
        } else if (raw == Optional.class || raw == io.vertx.core.Future.class) {
            return (arguments.length == 1) ? schema(arguments[0]) : new JsonObject();
        } else if (raw.isArray() && raw != byte[].class) {
            return array(raw.getComponentType());
        } else if (Collection.class.isAssignableFrom(raw) || Iterable.class == raw) {
            return array((arguments.length == 1) ? arguments[0] : Object.class);
        } else if (Map.class.isAssignableFrom(raw)) {
            return new JsonObject().put("type", "object")
                    .put("additionalProperties", (arguments.length == 2) ? schema(arguments[1]) : new JsonObject());
        }
        JsonObject scalar = scalar(raw);
        return (scalar != null) ? scalar : reference(raw);
    }

    private JsonObject array(Type items) {
        return new JsonObject().put("type", "array").put("items", schema(items));
    }

    private static JsonObject scalar(Class<?> type) {
        if (type == String.class || type == char.class || type == Character.class || CharSequence.class.isAssignableFrom(type)) {
            return type("string", null);
        } else if (type == boolean.class || type == Boolean.class) {
            return type("boolean", null);
        } else if (type == int.class || type == Integer.class || type == short.class || type == Short.class
                || type == byte.class || type == Byte.class) {
            return type("integer", "int32");
        } else if (type == long.class || type == Long.class) {
            return type("integer", "int64");
        } else if (type == float.class || type == Float.class) {
            return type("number", "float");
        } else if (type == double.class || type == Double.class || Number.class.isAssignableFrom(type)) {
            return type("number", "double");
        } else if (type == byte[].class) {
            return type("string", "byte");
        } else if (type == UUID.class) {
            return type("string", "uuid");
        } else if (type == Date.class || TemporalAccessor.class.isAssignableFrom(type)) {
            return type("string", "date-time");
        } else if (type.isEnum()) {
            JsonArray values = new JsonArray();
            Arrays.stream(type.getEnumConstants()).forEach(value -> values.add(((Enum<?>) value).name()));
            return type("string", null).put("enum", values);
        } else if (type == Object.class || type == JsonObject.class || type == JsonArray.class) {
            return new JsonObject();
        }
        return null;
    }

    private static JsonObject type(String type, String format) {
        JsonObject schema = new JsonObject().put("type", type);
        return (format == null) ? schema : schema.put("format", format);
    }

    private JsonObject reference(Class<?> type) {
        String name = name(type);

        // registered before the properties are read, a type that contains itself is referenced.
        if (!schemas.containsKey(name)) {
            schemas.put(name, new JsonObject());
            JsonObject properties = new JsonObject();
            properties(type).forEach((property, propertyType) -> properties.put(property, schema(propertyType)));
            schemas.put(name, new JsonObject().put("type", "object").put("properties", properties));
        }
        return new JsonObject().put("$ref", COMPONENTS + name);
    }

    private String name(Class<?> type) {
        String name = type.getSimpleName();
        Class<?> existing = names.putIfAbsent(name, type);

        if (existing != null && existing != type) {
            name = type.getName().replace('$', '.');
            names.put(name, type);
        }
        return name;
    }

    /**
     * The properties of a type as they are serialized: the components of a record, otherwise the public fields and the
     * getters, in the order they are declared.
     */
    private static Map<String, Type> properties(Class<?> type) {
        Map<String, Type> properties = new LinkedHashMap<>();

        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                properties.put(component.getName(), component.getGenericType());
            }
            return properties;
        }
        for (Field field : type.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                properties.put(field.getName(), field.getGenericType());
            }
        }
        Method[] methods = type.getMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));

        for (Method method : methods) {
            String property = property(method);
            if (property != null) {
                properties.putIfAbsent(property, method.getGenericReturnType());
            }
        }
        return properties;
    }

    private static String property(Method method) {
        String name = method.getName();
        String property = null;

        if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())
                && method.getDeclaringClass() != Object.class && method.getReturnType() != void.class) {
            if (name.startsWith("get") && name.length() > 3) {
                property = name.substring(3);
            } else if (name.startsWith("is") && name.length() > 2 && method.getReturnType() == boolean.class) {
                property = name.substring(2);
            }
        }
        return (property == null) ? null : Character.toLowerCase(property.charAt(0)) + property.substring(1);
    }
}
