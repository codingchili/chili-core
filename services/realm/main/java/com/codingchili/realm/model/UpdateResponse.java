package com.codingchili.realm.model;

import io.vertx.core.eventbus.Message;

import com.codingchili.core.protocol.*;

/**
 * @author Robin Duda
 */
public class UpdateResponse extends ProtocolMessage {

    public UpdateResponse(Message message) {
        super(message);
    }
}
