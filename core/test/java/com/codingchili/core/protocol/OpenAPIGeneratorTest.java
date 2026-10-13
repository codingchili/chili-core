package com.codingchili.core.protocol;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;

import static org.junit.Assert.*;

/**
 * Tests the generation of OpenAPI definitions from a protocol with typed routes.
 */
public class OpenAPIGeneratorTest {
    private JsonObject api;

    enum Level {NEW, ADMIN}

    record GetAccount(String id) {
    }

    record Node(String name, List<Node> children) {
    }

    record AccountView(String id, int age, long score, boolean active, Level level, Map<String, Double> weights,
                       List<String> tags, Node tree) {
    }

    record AccountStats(int count) {
    }

    @Roles(RoleMap.PUBLIC)
    @Address("accounts")
    @Description("Accounts.")
    public static class Handler implements Receiver<Request> {
        private final Protocol<Request> protocol = new Protocol<>(this);

        @Api
        @Description("Get an account.")
        public Future<AccountView> get(GetAccount input) {
            return Future.succeededFuture();
        }

        @Api(RoleMap.ADMIN)
        public AccountStats stats() {
            return new AccountStats(0);
        }

        @Api
        public void delete(GetAccount input) {
        }

        @Api
        public void legacy(Request request) {
        }

        @Override
        public void handle(Request request) {
            protocol.process(request);
        }
    }

    @Before
    public void setUp() {
        api = OpenAPIGenerator.toObject(new Protocol<>(new Handler()));
    }

    private JsonObject post(String route) {
        return api.getJsonObject("paths").getJsonObject("/" + route).getJsonObject("post");
    }

    private JsonObject schema(String name) {
        return api.getJsonObject("components").getJsonObject("schemas").getJsonObject(name);
    }

    @Test
    public void testInfoAndPaths() {
        assertEquals("3.0.0", api.getString("openapi"));
        assertEquals("accounts", api.getJsonObject("info").getString("title"));
        assertEquals("Accounts.", api.getJsonObject("info").getString("description"));
        assertEquals(Set.of("/get", "/stats", "/delete", "/legacy"), api.getJsonObject("paths").fieldNames());
    }

    @Test
    public void testInputIsTheRequestBody() {
        JsonObject get = post("get");
        assertEquals("Get an account.", get.getString("summary"));
        assertEquals("#/components/schemas/GetAccount", get.getJsonObject("requestBody").getJsonObject("content")
                .getJsonObject("application/json").getJsonObject("schema").getString("$ref"));
        assertEquals("string", schema("GetAccount").getJsonObject("properties").getJsonObject("id").getString("type"));
    }

    @Test
    public void testRouteWithoutInputHasNoRequestBody() {
        assertFalse(post("stats").containsKey("requestBody"));
        assertFalse(post("legacy").containsKey("requestBody"));
    }

    @Test
    public void testOutputIsTheResponseOrAnError() {
        var oneOf = post("get").getJsonObject("responses").getJsonObject("200").getJsonObject("content")
                .getJsonObject("application/json").getJsonObject("schema").getJsonArray("oneOf");
        assertEquals("#/components/schemas/AccountView", oneOf.getJsonObject(0).getString("$ref"));
        assertEquals("#/components/schemas/Error", oneOf.getJsonObject(1).getString("$ref"));
        assertEquals(6, schema("Error").getJsonObject("properties").getJsonObject("status").getJsonArray("enum").size());
    }

    @Test
    public void testVoidRouteIsAnsweredWithTheStatusOnly() {
        assertEquals("#/components/schemas/Error", post("delete").getJsonObject("responses").getJsonObject("200")
                .getJsonObject("content").getJsonObject("application/json").getJsonObject("schema").getString("$ref"));
    }

    @Test
    public void testRolesAreListed() {
        assertEquals("admin", post("stats").getJsonArray("x-roles").getString(0).toLowerCase());
    }

    @Test
    public void testPropertyTypes() {
        JsonObject properties = schema("AccountView").getJsonObject("properties");
        assertEquals("int32", properties.getJsonObject("age").getString("format"));
        assertEquals("int64", properties.getJsonObject("score").getString("format"));
        assertEquals("boolean", properties.getJsonObject("active").getString("type"));
        assertEquals(List.of("NEW", "ADMIN"), properties.getJsonObject("level").getJsonArray("enum").getList());
        assertEquals("number", properties.getJsonObject("weights").getJsonObject("additionalProperties").getString("type"));
        assertEquals("string", properties.getJsonObject("tags").getJsonObject("items").getString("type"));
    }

    @Test
    public void testRecursiveTypesAreReferenced() {
        assertEquals("#/components/schemas/Node", schema("AccountView").getJsonObject("properties")
                .getJsonObject("tree").getString("$ref"));
        assertEquals("#/components/schemas/Node", schema("Node").getJsonObject("properties")
                .getJsonObject("children").getJsonObject("items").getString("$ref"));
    }

    @Test
    public void testYamlAndJsonAreGenerated() {
        Protocol<Request> protocol = new Protocol<>(new Handler());
        assertTrue(OpenAPIGenerator.toYaml(protocol).contains("openapi: \"3.0.0\"")
                || OpenAPIGenerator.toYaml(protocol).contains("openapi: 3.0.0"));
        assertEquals(api, new JsonObject(OpenAPIGenerator.toJson(protocol)));
    }

    @Test
    public void testRouteDocumentationContainsTheOutput() {
        Route<?> route = new Protocol<>(new Handler()).getSchema().getRoutes().get("stats");
        assertEquals("int", route.getOutput().get("count"));
    }

}
