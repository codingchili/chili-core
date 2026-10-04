package com.codingchili.core.benchmarking;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.CoreContext;

import static com.codingchili.core.configuration.CoreStrings.getSemaphoreTimeout;

/**
 * An asynchronous semaphore.
 * <p>
 * This implementation does not make sure that a principal releasing
 * a permit actually owned the permit.
 */
public class AsynchronousSemaphore {
    private final Queue<SemaphoreWaiter> waiters = new LinkedList<>();
    private final CoreContext context;
    private final int limit;
    private int permits;

    /**
     * Creates a new asynchronous semaphore.
     *
     * @param context the context on which the semaphore operates.
     * @param limit   the maximum number of permits to allow concurrently.
     */
    public AsynchronousSemaphore(CoreContext context, int limit) {
        this.context = context;
        this.limit = limit;
        this.permits = limit;
    }

    /**
     * Acquires a permit from the semaphore when ones becomes available.
     * If the timeout is expired while waiting the future will fail.
     *
     * @param timeoutMS the maximum time to wait for a permit.
     * @return future completed when a permit is received.
     */
    public synchronized Future<Void> acquire(int timeoutMS) {
        Promise<Void> promise = Promise.promise();

        if (permits == 0) {
            SemaphoreWaiter waiter = new SemaphoreWaiter(promise);
            waiters.add(waiter);
            context.timer(timeoutMS, event -> {
                waiter.expired.set(true);
                promise.tryFail(getSemaphoreTimeout(timeoutMS));
            });
        } else if (permits > 0) {
            permits--;
            promise.complete();
        }
        return promise.future();
    }

    /**
     * Returns a permit to the semaphore without checking that the
     * principal returning a permit actually owns a permit.
     */
    public synchronized void release() {
        if (permits < limit) {
            permits++;
            SemaphoreWaiter waiter;
            while (permits > 0 && (waiter = waiters.poll()) != null) {
                if (!waiter.expired.get()) {
                    if (waiter.promise.tryComplete()) {
                        permits--;
                    }
                }
            }
        }
    }

    private static class SemaphoreWaiter {
        private final AtomicBoolean expired = new AtomicBoolean(false);
        private final Promise<Void> promise;

        SemaphoreWaiter(Promise<Void> promise) {
            this.promise = promise;
        }
    }
}
