package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.storage.QueryBuilder;
import io.vertx.core.Future;

/**
 * An interface which defines the functionality of a session provider.
 */
public interface SessionFactory {

    /**
     * Creates a new session.
     *  @param source     the listener that created the session, the listener must
     *                   also be able to route asynchronous messages back to the connection
     *                   using the eventbus for example.
     * @param connection a unique identification of the sessions connection.*/
    Future<Session> create(String source, String connection);

    /**
     * Updates the data stored in a session.
     *
     * @param session the session to be updated.
     * @return callback
     */
    Future<Void> update(ClusteredSession session);

    /**
     * Destroys a session.
     *
     * @param session the session to be destroyed.
     * @return callback
     */
    Future<Void> destroy(Session session);

    /**
     * Checks if a session is active.
     *
     * @param session the session to check if active.
     * @return callback.
     */
    Future<Boolean> isActive(Session session);

    /**
     * Constructs a query that can be used to search for existing sessions.
     *
     * @param attribute the name of the attribute to query on.
     * @return a fluent query builder.
     */
    QueryBuilder<ClusteredSession> query(String attribute);

    /**
     * @return the context of the session factory.
     */
    CoreContext context();
}
