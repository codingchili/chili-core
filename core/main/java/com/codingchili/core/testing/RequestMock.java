package com.codingchili.core.testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.transport.ClusterRequest;

/**
 * @author Robin Duda
 * <p>
 * Mocked request object.
 */
public abstract class RequestMock {

    public static ClusterRequestMock get(ResponseListener listener) {
        return get("", listener);
    }

    public static ClusterRequestMock get(String route, ResponseListener listener) {
        return get(route, listener, new JsonObject());
    }

    public static ClusterRequestMock get(String route, ResponseListener listener, JsonObject json) {
        return new ClusterRequestMock(new MessageMock(route, listener, json));
    }

    private static class ClusterRequestMock extends ClusterRequest {
        ClusterRequestMock(MessageMock message) {
            super(message);
        }
    }
}
