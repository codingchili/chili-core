package com.codingchili.core.protocol;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Basic form of a request response.
 */
public abstract class ProtocolMessage {
    private JsonObject data;

    public ProtocolMessage(Message message) {
        this.data = (JsonObject) message.body();
    }

    public boolean is(ResponseStatus status) {
        return status().equals(status);
    }

    public ResponseStatus status() {
        if (data.containsKey(ID_STATUS)) {
            return ResponseStatus.valueOf(data.getString(ID_STATUS));
        } else {
            return ResponseStatus.ERROR;
        }
    }

    public String message() {
        if (data.containsKey(ID_MESSAGE)) {
            return data.getString(ID_MESSAGE);
        } else {
            return "";
        }
    }

    public JsonObject data() {
        return data;
    }
}
