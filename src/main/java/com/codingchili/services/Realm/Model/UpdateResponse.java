package com.codingchili.services.Realm.Model;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Protocol.*;

import static com.codingchili.core.Protocol.ResponseStatus.ERROR;

/**
 * @author Robin Duda
 */
public class UpdateResponse extends ProtocolMessage {

    public static UpdateResponse from(AsyncResult<Message<Object>> message) {
        if (message.succeeded()) {
            return Serializer.unpack((JsonObject) message.result().body(), UpdateResponse.class);
        } else {
            UpdateResponse response = new UpdateResponse();
            response.setStatus(ERROR);
            return response;
        }
    }
}
