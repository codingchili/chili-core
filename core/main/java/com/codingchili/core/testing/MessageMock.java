package com.codingchili.core.testing;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * Mock class for messages.
 */
public class MessageMock implements Message<Object> {
    private ResponseListener listener;
    private JsonObject json;

    /**
     * Creates a new message mock.
     *
     * @param json the payload of the message.
     */
    public MessageMock(JsonObject json) {
        if (json == null) {
            this.json = new JsonObject();
        } else {
            this.json = json;
        }
    }

    public MessageMock setListener(ResponseListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public Object body() {
        return json;
    }

    @Override
    public void reply(Object message) {
        JsonObject data = new JsonObject();

        if (message instanceof JsonObject) {
            data = (JsonObject) message;
        } else if (message instanceof Buffer) {
            // normally buffers passed directly, for testing purposes
            // its wrapped in a json object.
            try {
                data = ((Buffer) message).toJsonObject();

                if (!data.containsKey(PROTOCOL_STATUS)) {
                    data.put(PROTOCOL_STATUS, ACCEPTED);
                }
            } catch (DecodeException e) {
                data.put(ID_BUFFER, message.toString());
                data.put(PROTOCOL_STATUS, ACCEPTED);
            }
        }
        if (listener != null) {
            listener.handle(data, responseStatusFromJson(data));
        }
    }

    private ResponseStatus responseStatusFromJson(JsonObject json) {
        return ResponseStatus.valueOf(json.getString(PROTOCOL_STATUS));
    }

    @Override
    public String address() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultiMap headers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String replyAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSend() {
        return false; // publish to all listeners for tests.
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