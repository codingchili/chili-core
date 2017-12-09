package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.storage.*;
import io.vertx.core.Future;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates new sessions and manages existing.
 */
public class ClusteredSessionFactory implements SessionFactory<ClusteredSession> {
    private static AtomicBoolean loading = new AtomicBoolean();
    private static Future<Void> loader = Future.future();
    private static Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private static ClusteredSessionFactory storage;
    private static AsyncStorage<ClusteredSession> sessions;
    private static CoreContext core;
    private static Logger logger;

    public static Future<ClusteredSessionFactory> get(CoreContext core) {
        Future<ClusteredSessionFactory> future = Future.future();

        if (loader.isComplete()) {
            future.complete(storage);
        } else {
            queue.add(() -> future.complete(storage));

            if (!loading.getAndSet(true)) {
                loader.setHandler(done -> queue.forEach(Runnable::run));
                ClusteredSessionFactory.core = core;
                create(loader);
            }
        }
        return future;
    }

    private ClusteredSessionFactory(AsyncStorage<ClusteredSession> sessions) {
        ClusteredSessionFactory.sessions = sessions;
        logger = sessions.context().logger(getClass());
    }

    @Override
    public Future<Boolean> isActive(ClusteredSession session) {
        Future<Boolean> future = Future.future();
        sessions.contains(session.getId(), future);
        return future;
    }

    @Override
    public QueryBuilder<ClusteredSession> query(String attribute) {
        Query<ClusteredSession> query = Query.on(attribute);
        query.storage(sessions);
        query.mapper((session) -> session.setSessionFactory(this));
        return query;
    }

    @Override
    public Future<Void> destroy(ClusteredSession session) {
        Future<Void> future = Future.future();
        sessions.remove(session.getId(), future);
        return future;
    }

    @Override
    public Future<Void> update(ClusteredSession session) {
        Future<Void> future = Future.future();
        sessions.put(session, future);
        return future;
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
        Future<ClusteredSession> future = Future.future();
        ClusteredSession session = new ClusteredSession(this, home, id);

        update(session).setHandler(update -> {
            if (update.failed()) {
                future.fail(update.cause());
            } else {
                future.complete(session);
            }
        });

        return future;
    }

    private static void create(Future<Void> future) {
        new StorageLoader<ClusteredSession>(core)
                .withPlugin(getPluginWithSelector())
                .withValue(ClusteredSession.class)
                .build(load -> {
                    if (load.succeeded()) {
                        storage = new ClusteredSessionFactory(load.result());
                        future.complete();
                    } else {
                        future.fail(load.cause());
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
