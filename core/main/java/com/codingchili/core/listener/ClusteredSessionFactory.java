package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.storage.*;
import io.vertx.core.Future;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Creates new sessions and manages existing.
 */
public class ClusteredSessionFactory implements SessionFactory {
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
    public Future<Boolean> isActive(Session session) {
        Future<Boolean> future = Future.future();
        sessions.contains(session.id(), future);
        return future;
    }

    @Override
    public QueryBuilder<ClusteredSession> query(String attribute) {
        return sessions.query(attribute);
    }

    @Override
    public Future<Void> destroy(Session session) {
        Future<Void> future = Future.future();
        sessions.remove(session.id(), future);
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
    public Future<Session> create(String source, String connection) {
        Future<Session> future = Future.future();
        ClusteredSession session = new ClusteredSession(this, source, connection);

        update(session).setHandler(update -> {
            if (update.failed()) {
                logger.onError(update.cause());
            }
        });

        future.complete(session);
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
