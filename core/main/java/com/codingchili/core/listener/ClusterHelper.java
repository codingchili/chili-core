package com.codingchili.core.listener;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;

/**
 * Helper class to send clustered messages.
 */
public class ClusterHelper {

    /**
     * Sends a message over the cluster.
     *
     * @param message a message to reply to.
     * @param object  an object to be converted to an event bus compliant object (buffer or jsonobject).
     */
    public static void reply(Message message, Object object) {
        if (object instanceof Buffer) {
            message.reply(object);
        } else {
            JsonObject reply;

            if (object instanceof JsonObject) {
                reply = (JsonObject) object;
            } else {
                reply = Serializer.json(object);
            }
            if (!reply.containsKey(PROTOCOL_STATUS)) {
                reply.put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED);
            }
            message.reply(reply);
        }
    }
}
