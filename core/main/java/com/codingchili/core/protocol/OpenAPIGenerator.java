package com.codingchili.core.protocol;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Template for generating valid OpenAPI 3 definitions, see https://apidevtools.org/swagger-parser/online/
 */
public class OpenAPIGenerator {

    /**
     * @param protocol the protocol instance to generate an API definition from.
     * @return a YAML OpenAPI v3 schema generated from a protocol.
     * @see #toJson(Protocol)
     */
    public static String toYaml(Protocol<?> protocol) {
        return Serializer.yaml(new JsonObject(toJson(protocol)));
    }

    /**
     * Generates OpenAPI v3 compatible definitions from the given protocol. The information
     * is retrieved using protocol metadata, registered using the documentation API or with
     * protocol annotations. Reflection may also be used to retrieve additional information.
     *
     * @param protocol the protocol instance to generate an API definition from.
     * @return a YAML OpenAPI v3 schema generated from a protocol.
     */
    public static String toJson(Protocol<?> protocol) {
        JsonObject swagger = new JsonObject()
                .put("openapi", "3.0.0")
                .put("info", new JsonObject()
                        .put("description", "")
                        .put("version", "1.0.0")
                        .put("title", "title")
                );

        swagger.put("tags", new JsonArray()
                .add(new JsonObject()
                        .put("name", "metadata")
                        .put("description", "is wack"))
        );

        swagger.put("paths", new JsonObject()
                // repeat for each route in protocol
                .put("/", new JsonObject()
                        // always use post here.
                        .put("post", new JsonObject()
                                .put("tags", new JsonArray().add("metadata"))
                                .put("operationId", "something something")
                                .put("summary", "description")
                                .put("responses", new JsonObject()
                                        // repeat for each ResponseStatus, status validated with [0-9X]{3}
                                        .put("XX" + ResponseStatus.ACCEPTED.ordinal(),
                                                createSchemaModelFromClass(swagger, null)
                                                        .put("description", ResponseStatus.ACCEPTED.name()))
                                )
                                .put("requestBody", createSchemaModelFromClass(swagger, null)
                                        .put("required", true))
                        )
                )
        );
        return swagger.encodePrettily();
    }

    private static JsonObject createSchemaModelFromClass(JsonObject swagger, Class theClass) {
        return new JsonObject()
                .put("description", "")
                .put("content", createContentFromClass(swagger, theClass));
    }

    private static JsonObject createContentFromClass(JsonObject swagger, Class theClass) {
        return new JsonObject()
                // default to application/json for now.
                .put("application/json", new JsonObject()
                        .put("schema", new JsonObject()
                                .put("$ref", createModelReferenceFromClass(swagger, null))
                        )
                );
    }

    private static String createModelReferenceFromClass(JsonObject swagger, Class theClass) {
        // if model reference already exists for given class skip.
        swagger.put("components", new JsonObject()
                .put("schemas", new JsonObject()
                        .put("datasetList", new JsonObject()
                                .put("type", "object")
                                .put("properties", describeObjectModelFromClass(theClass)))
                )
        );
        return "#/components/schemas/datasetList";
    }

    private static JsonObject describeObjectModelFromClass(Class theClass) {
        return new JsonObject()
                // iterate all properties of class recursively and add them.
                .put("propertyName", new JsonObject()
                        .put("description", "")
                        .put("type", "string"));
    }

    public static void main(String[] args) {
        System.out.println(toYaml(null));
    }
}
