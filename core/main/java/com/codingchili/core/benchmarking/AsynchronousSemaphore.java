package com.codingchili.core.benchmarking;

import com.codingchili.core.context.CoreContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codingchili.core.configuration.CoreStrings.getSemaphoreTimeout;

/**
 * @author Robin Duda
 * <p>
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
     * @param handler   the handler to be called when a permit is received.
     * @param timeoutMS the maximum time to wait for a permit.
     */
    public synchronized void acquire(Handler<AsyncResult<Void>> handler, int timeoutMS) {
        Future<Void> future = Future.<Void>future().setHandler(handler);
        if (permits == 0) {
            SemaphoreWaiter waiter = new SemaphoreWaiter(future);
            waiters.add(waiter);
            context.timer(timeoutMS, event -> {
                waiter.expired.set(true);
                future.tryFail(getSemaphoreTimeout(timeoutMS));
            });
        } else if (permits > 0) {
            permits--;
            future.complete();
        }
    }

    /**
     * Returns a permit to the semaphore without checking that the
     * principal returning a permit actually owns a permit.
     */
    public synchronized void release() {
        if (permits < limit) {
            permits++;
            SemaphoreWaiter waiter;
            while ((waiter = waiters.poll()) != null && permits > 0) {
                if (!waiter.expired.get()) {
                    if (waiter.future.tryComplete()) {
                        permits--;
                    }
                }
            }
        }
    }

    private class SemaphoreWaiter {
        private Future<Void> future;
        private AtomicBoolean expired = new AtomicBoolean(false);

        SemaphoreWaiter(Future<Void> future) {
            this.future = future;
        }
    }
}
