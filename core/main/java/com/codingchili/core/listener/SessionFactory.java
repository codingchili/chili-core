package com.codingchili.core.listener;

import io.vertx.core.Future;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.storage.QueryBuilder;

/**
 * An interface which defines the functionality of a session provider.
 */
public interface SessionFactory<T extends Session> {

    /**
     * Creates a new session.
     *
     * @param home event bus messages will be sent to this address.
     * @return callback
     */
    Future<T> create(String home);

    /**
     * Creates a new session.
     *
     * @param home event bus messages will be sent to this address.
     * @param id   the id of the session, for identification.
     * @return callback
     */
    Future<T> create(String home, String id);

    /**
     * Updates the data stored in a session.
     *
     * @param session the session to be updated.
     * @return callback
     */
    Future<Void> update(T session);

    /**
     * Destroys a session.
     *
     * @param session the session to be destroyed.
     * @return callback
     */
    Future<Void> destroy(T session);

    /**
     * Checks if a session is active.
     *
     * @param session the session to check if active.
     * @return callback.
     */
    Future<Boolean> isActive(T session);

    /**
     * Constructs a query that can be used to search for existing sessions.
     *
     * @param attribute the name of the attribute to query on.
     * @return a fluent query builder.
     */
    QueryBuilder<T> query(String attribute);

    /**
     * @return the context of the session factory.
     */
    CoreContext context();
}
