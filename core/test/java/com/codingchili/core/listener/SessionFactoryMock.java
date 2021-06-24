package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.*;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.storage.QueryBuilder;

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
    public Future<ClusteredSession> create(String home) {
        return create(home, UUID.randomUUID().toString());
    }

    @Override
    public Future<ClusteredSession> create(String home, String connectionId) {
        Promise<ClusteredSession> promise = Promise.promise();
        ClusteredSession session = new ClusteredSession(this, home, connectionId);
        sessions.put(session.getId(), session);
        promise.complete(session);
        return promise.future();
    }

    @Override
    public Future<Void> update(ClusteredSession session) {
        Promise<Void> promise = Promise.promise();
        sessions.put(session.getId(), session);
        return promise.future();
    }

    @Override
    public Future<Void> destroy(ClusteredSession session) {
        Promise<Void> promise = Promise.promise();
        sessions.remove(session.getId());
        return promise.future();
    }

    @Override
    public Future<Boolean> isActive(ClusteredSession session) {
        Promise<Boolean> promise = Promise.promise();
        promise.complete(sessions.containsKey(session.getId()));
        return promise.future();
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
