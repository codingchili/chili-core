package com.codingchili.core.listener;

import com.codingchili.core.storage.Storable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Indicates that a request is part of a persistent connection. This means
 * that services can write to the session without using request-reply messaging.
 */
public interface Session extends Messageable, Storable {
    String CONNECTION = "connection";
    String SOURCE = "source";

    /**
     * @return true if the session is still active.
     */
    Future<Boolean> isActive();

    /**
     * Destroys the session.
     * @return callback
     */
    Future<Void> destroy();

    /**
     * @return an id identifying the listener that is the session owner.
     */
    String source();

    /**
     * @return the id of the connection that the #{@link #source } owner manages.
     */
    String connection();

    /**
     * @return returns data associated with a session.
     */
    JsonObject data();

    /**
     * Call this to update the session data after modification.
     * @return callback
     */
    Future<Void> update();
}
