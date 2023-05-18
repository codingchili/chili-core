package com.codingchili.core.listener;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.storage.*;

/**
 * Creates new sessions and manages existing.
 */
public class ClusteredSessionFactory implements SessionFactory<ClusteredSession> {
    private static AtomicBoolean loading = new AtomicBoolean();
    private static Promise<Void> loader = Promise.promise();
    private static Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static ClusteredSessionFactory storage;
    private static AsyncStorage<ClusteredSession> sessions;
    private static CoreContext core;
    private static Logger logger;

    public static Future<ClusteredSessionFactory> get(CoreContext core) {
        Promise<ClusteredSessionFactory> promise = Promise.promise();

        if (loader.future().isComplete()) {
            promise.complete(storage);
        } else {
            queue.add(() -> promise.complete(storage));

            if (!loading.getAndSet(true)) {
                loader.future().onComplete(done -> queue.forEach(Runnable::run));
                ClusteredSessionFactory.core = core;
                create(loader);
            }
        }
        return promise.future();
    }

    private ClusteredSessionFactory(AsyncStorage<ClusteredSession> sessions) {
        ClusteredSessionFactory.sessions = sessions;
        logger = sessions.context().logger(getClass());
    }

    @Override
    public Future<Boolean> isActive(ClusteredSession session) {
        Promise<Boolean> promise = Promise.promise();
        sessions.contains(session.getId(), promise);
        return promise.future();
    }

    @Override
    public QueryBuilder<ClusteredSession> query(String attribute) {
        Query<ClusteredSession> query = new Query<ClusteredSession>().on(attribute);
        query.storage(sessions);
        query.mapper((session) -> session.setSessionFactory(this));
        return query;
    }

    @Override
    public Future<Void> destroy(ClusteredSession session) {
        Promise<Void> promise = Promise.promise();
        sessions.remove(session.getId(), promise);
        return promise.future();
    }

    @Override
    public Future<Void> update(ClusteredSession session) {
        Promise<Void> promise = Promise.promise();
        sessions.put(session, promise);
        return promise.future();
    }

    @Override
    public CoreContext context() {
        return core;
    }

    @Override
    public Future<ClusteredSession> create(String home) {
        return create(home, UUID.randomUUID().toString());
    }

    @Override
    public Future<ClusteredSession> create(String home, String id) {
        Promise<ClusteredSession> promise = Promise.promise();
        ClusteredSession session = new ClusteredSession(this, home, id);

        update(session).onComplete(update -> {
            if (update.failed()) {
                promise.fail(update.cause());
            } else {
                promise.complete(session);
            }
        });

        return promise.future();
    }

    private static void create(Promise<Void> promise) {
        new StorageLoader<ClusteredSession>(core)
                .withPlugin(getPluginWithSelector())
                .withValue(ClusteredSession.class)
                .build(load -> {
                    if (load.succeeded()) {
                        storage = new ClusteredSessionFactory(load.result());
                        promise.complete();
                    } else {
                        promise.fail(load.cause());
                    }
                });
    }

    private static Class<? extends AsyncStorage> getPluginWithSelector() {
        if (Configurations.launcher().isClustered()) {
            return HazelMap.class;
        } else {
            return SharedMap.class;
        }
    }
}
