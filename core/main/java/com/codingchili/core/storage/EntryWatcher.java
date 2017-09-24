package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.context.TimerSource;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Robin Duda
 * <p>
 * Periodically executes a reusable query.
 * <p>
 * May be used as a near-cache.
 */
public class EntryWatcher<Value extends Storable> {
    private AtomicBoolean active = new AtomicBoolean(false);
    private Consumer<Collection<Value>> consumer = (collection) -> {
    };
    private Supplier<QueryBuilder<Value>> query;
    private AsyncStorage<Value> storage;
    private StorageContext context;
    private TimerSource timer;

    /**
     * Creates a new (paused) entry watcher on the given storage by executing
     * the given query at intervals given by the timer.
     *
     * @param storage the storage to watch entries in
     * @param query   the query to be executed
     * @param timer   interval of the query executions
     */
    public EntryWatcher(AsyncStorage<Value> storage, Supplier<QueryBuilder<Value>> query, TimerSource timer) {
        this.context = storage.context();
        this.storage = storage;
        this.query = query;
        this.timer = timer;
    }

    /**
     * Changes the query of the entry watcher.
     *
     * @param query the new query to use
     * @return fluent
     */
    public EntryWatcher<Value> setQuery(QueryBuilder<Value> query) {
        this.query = () -> query;
        return this;
    }

    /**
     * Changes the timer source of the entry watcher.
     *
     * @param timer the new timer source to use
     * @return fluent
     */
    public EntryWatcher<Value> setTimer(TimerSource timer) {
        this.timer = timer;
        return this;
    }

    /**
     * Changes the consumer that receives entries.
     *
     * @param consumer the new consumer to handle results
     * @return fluent
     */
    public EntryWatcher<Value> setConsumer(Consumer<Collection<Value>> consumer) {
        this.consumer = consumer;
        return this;
    }

    /**
     * Starts the entry watcher with the given consumer.
     *
     * @param consumer the consumer that receives the results of the query.
     * @return fluent
     */
    public EntryWatcher<Value> start(Consumer<Collection<Value>> consumer) {
        this.consumer = consumer;
        active.set(true);

        context.periodic(timer, getClass().getName(), (handler) -> {
            if (active.get()) {
                execute();
            }
        });
        return this;
    }

    private void execute() {
        QueryBuilder<Value> query = this.query.get();
        query.execute(q -> {
            if (q.succeeded()) {
                consumer.accept(q.result());
                context.onWatcherCompleted(query.name(), q.result().size());
            } else {
                context.onWatcherFailed(query.name(), q.cause().getMessage());
            }
        });
    }

    /**
     * Sets the watcher into a paused state.
     */
    public void pause() {
        active.set(false);
        context.onWatcherPaused(query.get().name());
    }

    /**
     * Sets the watcher into the resumed state.
     */
    public void resume() {
        active.set(true);
        context.onWatcherResumed(query.get().name());
    }

    /**
     * Returns the state of the watcher.
     *
     * @return true if the watcher is not paused.
     */
    public boolean isActive() {
        return active.get();
    }
}
