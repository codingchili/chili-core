package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.storage.Storable;

/**
 * Indicates that a request is part of a persistent connection. This means
 * that services can write to the session without using request-reply messaging.
 */
public interface Session extends Messageable, Storable {
    String ID = "id";
    String HOME = "home";

    /**
     * @return true if the session is still active.
     */
    Future<Boolean> isActive();

    /**
     * @return the session owner identified by a string.
     */
    String getHome();

    /**
     * @return returns data associated with a session.
     */
    JsonObject asJson();

    /**
     * Destroys the session.
     *
     * @return callback
     */
    Future<Void> destroy();

    /**
     * Call this to update the session data after modification.
     *
     * @return callback
     */
    Future<Void> update();
}
