package com.codingchili.core.protocol;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * tests for the api documentation api.
 */
@RunWith(VertxUnitRunner.class)
public class ProtocolDescriptionTest {

    @Test
    public void programmaticallyDescribeProtocol() {
        ProtocolDescription<TestPayload> protocol = new ProtocolDescription<>();

        protocol.setDescription("a testing api")
                .setTarget("master")
                .setTemplate(Authentication.class);

        Route<TestPayload> route = new Route<TestPayload>("info")
                .setDescription("retrieves the info")
                .setRoles(Role.ADMIN)
                .setTemplate(TestPayload.class);

        protocol.addRoute(route);

        // verify that it's serializable.
        String yaml = Serializer.yaml(protocol);

        // verify that it's also deserializable.
        Serializer.unyaml(yaml, ProtocolDescription.class);
    }

    private class Authentication {
        public String token;
    }

    private class TestPayload {
        public String details;
    }
}