package com.codingchili.core.Shared;

import com.codingchili.core.Protocols.ClusterRequest;
import com.codingchili.core.Protocols.ResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.Configuration.Strings.ID_ACTION;
import static com.codingchili.core.Configuration.Strings.PROTOCOL_STATUS;

/**
 * @author Robin Duda
 */
public class RequestMock {

    public static ClusterRequestMock get(String action, ResponseListener listener, JsonObject json) {
        return new ClusterRequestMock(new MessageMock(listener, json, action));
    }

    private static class ClusterRequestMock extends ClusterRequest {
        ClusterRequestMock(MessageMock message) {
            super(message);
        }
    }

    private static class MessageMock implements Message {
        private JsonObject json;
        private ResponseListener listener;

        MessageMock(ResponseListener listener, JsonObject json, String action) {
            if (json == null) {
                this.json = new JsonObject();
            } else {
                this.json = json;
            }

            this.json.put(ID_ACTION, action);
            this.listener = listener;
        }

        @Override
        public Object body() {
            return json;
        }

        @Override
        public void reply(Object message) {
            JsonObject data = (JsonObject) message;
            listener.handle(data, ResponseStatus.valueOf(data.getString(PROTOCOL_STATUS)));
        }


        @Override
        public String address() {
            return null;
        }

        @Override
        public MultiMap headers() {
            return null;
        }

        @Override
        public String replyAddress() {
            return null;
        }

        @Override
        public void reply(Object message, DeliveryOptions options) {

        }

        @Override
        public void fail(int failureCode, String message) {

        }

        @Override
        public void reply(Object message, DeliveryOptions options, Handler replyHandler) {

        }

        @Override
        public void reply(Object message, Handler replyHandler) {

        }
    }
}
