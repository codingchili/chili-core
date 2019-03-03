package com.codingchili.core.testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.transport.ClusterRequest;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
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
        json.put(PROTOCOL_ROUTE, route);

        return new ClusterRequestMock(new MessageMock(json)
                .setListener(listener));
    }

    public static class ClusterRequestMock extends ClusterRequest {
        private MessageMock message;

        ClusterRequestMock(MessageMock message) {
            super(message);
            this.message = message;
        }

        /**
         * @param listener invoked when the request response is written.
         * @return fluent.
         */
        public ClusterRequestMock setListener(ResponseListener listener) {
            message.setListener(listener);
            return this;
        }

        /**
         * @param target overwrite the current target with the given value.
         * @return fluent.
         */
        public ClusterRequestMock setTarget(String target) {
            data().put(PROTOCOL_TARGET, target);
            return this;
        }

        /**
         * @param route overwrite the current route with the given value.
         * @return fluent.
         */
        public ClusterRequestMock setRoute(String route) {
            data().put(PROTOCOL_ROUTE, route);
            return this;
        }
    }
}
