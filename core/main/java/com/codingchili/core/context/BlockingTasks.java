package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of the blocking tasks that have been submitted through a context, so that a shutdown
 * can wait for them to complete: closing vertx interrupts the tasks that are still running.
 */
class BlockingTasks {
    private final List<Promise<Void>> waiting = new ArrayList<>();
    private int running = 0;

    /**
     * Registers a task that is about to be submitted, {@link #end()} must be called when it completes.
     */
    synchronized void begin() {
        running++;
    }

    /**
     * Registers that a task has completed, successfully or not.
     */
    void end() {
        List<Promise<Void>> completed;

        synchronized (this) {
            running--;
            if (running > 0) {
                return;
            }
            completed = new ArrayList<>(waiting);
            waiting.clear();
        }
        completed.forEach(Promise::tryComplete);
    }

    /**
     * @return the number of tasks that have been submitted and have not completed.
     */
    synchronized int running() {
        return running;
    }

    /**
     * Waits for the running tasks to complete.
     *
     * @param vertx     used to time out.
     * @param timeoutMS the maximum time to wait.
     * @return a future that is completed when no tasks are running or when the time is up, whichever comes first.
     * Check {@link #running()} to see if all tasks completed.
     */
    Future<Void> await(Vertx vertx, long timeoutMS) {
        Promise<Void> promise = Promise.promise();

        synchronized (this) {
            if (running == 0 || timeoutMS <= 0) {
                return Future.succeededFuture();
            }
            waiting.add(promise);
        }
        vertx.setTimer(timeoutMS, timer -> promise.tryComplete());
        return promise.future();
    }
}
