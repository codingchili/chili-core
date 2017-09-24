package com.codingchili.realm.model;

import com.codingchili.core.protocol.ProtocolMessage;
import io.vertx.core.eventbus.Message;

/**
 * @author Robin Duda
 */
public class UpdateResponse extends ProtocolMessage {

    public UpdateResponse(Message message) {
        super(message);
    }
}
