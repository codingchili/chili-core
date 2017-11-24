package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.storage.QueryBuilder;
import io.vertx.core.Future;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of a session store.
 */
public class SessionFactoryMock implements SessionFactory<ClusteredSession> {
    public static Map<String, Session> sessions = new HashMap<>();
    private CoreContext core;

    public SessionFactoryMock(CoreContext core) {
        this.core = core;
    }

    @Override
    public Future<ClusteredSession> create(String source, String connection) {
        Future<ClusteredSession> future = Future.future();
        ClusteredSession session = new ClusteredSession(this, source, connection);
        sessions.put(session.id(), session);
        future.complete(session);
        return future;
    }

    @Override
    public Future<Void> update(ClusteredSession session) {
        Future<Void> future = Future.future();
        sessions.put(session.id(), session);
        return future;
    }

    @Override
    public Future<Void> destroy(ClusteredSession session) {
        Future<Void> future = Future.future();
        sessions.remove(session.id());
        return future;
    }

    @Override
    public Future<Boolean> isActive(ClusteredSession session) {
        Future<Boolean> future = Future.future();
        future.complete(sessions.containsKey(session.id()));
        return future;
    }

    @Override
    public QueryBuilder<ClusteredSession> query(String attribute) {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public CoreContext context() {
        return core;
    }
}
