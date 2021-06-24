package com.codingchili.core.listener;

import io.vertx.core.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

/**
 * The MultiHandler is capable of routing a request into any
 * of the given handlers using the #{@link Request#target()}. The requests
 * target should match the address of the handler. If no handlers matching
 * the processed request is found, then a #{@link HandlerMissingException}
 * is thrown.
 */
public class MultiHandler implements CoreHandler {
    private Map<String, CoreHandler> map = new HashMap<>();
    private AtomicBoolean started = new AtomicBoolean(false);
    private CoreContext core;
    private String address;

    /**
     * @param handlers same as @see #MultiHandler(CoreHandler...)
     */
    public MultiHandler(List<CoreHandler> handlers) {
        handlers.forEach(this::add);
    }

    /**
     * @param handlers a list of handlers to mount on the multi-handler.
     *                 When a request is processed the requests target
     *                 will be used to lookup a CoreHandler with a matching address.
     */
    public MultiHandler(CoreHandler... handlers) {
        for (CoreHandler handler : handlers) {
            add(handler);
        }
    }

    /**
     * Set the address to use - required when deployed in a cluster listener.
     *
     * @param address the address to listen on.
     * @return fluent.
     */
    public MultiHandler setAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Adds a sub-handler to the MultiHandler, may be called when the MultiHandler
     * is already deployed - but then it requires the handlers address not to be registered.
     *
     * @param handler the handler to add.
     * @return a Future that will be completed when the handler is started if the MultiHandler
     * is already deployed. If the MultiHandler is not deployed - the future is completed.
     */
    public Future<Void> add(CoreHandler handler) {
        Promise<Void> promise = Promise.promise();

        // if already started - start up the handler.
        if (started.get()) {
            if (map.containsKey(handler.address())) {
                throw new CoreRuntimeException("A deployed handler already exists with address: " + handler.address());
            } else {
                handler.init(core);
                handler.start(promise);
            }
        }
        map.put(handler.address(), handler);
        promise.complete();
        return promise.future();
    }

    /**
     * Stops the given handler.
     *
     * @param address address of the handler to be removed - if the MultiHandler is started then
     *                the given handler will be stopped.
     * @return a future that is completed when the handler is removed. If the multihandler is
     * not yet started - then the future will be completed.
     */
    public Future<Void> remove(String address) {
        Promise<Void> promise = Promise.promise();

        if (started.get()) {
            // we are started and the handler exists.
            if (map.containsKey(address)) {
                map.get(address).stop(promise);
                map.remove(address);
                return promise.future();
            }
        }
        promise.complete();
        return promise.future();
    }

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Promise<Void> start) {
        started.getAndSet(true);
        forAll((handler, future) -> {
            handler.init(core);
            handler.start(future);
        }).onComplete(start);
    }

    private Future<Void> forAll(BiConsumer<CoreHandler, Promise<Void>> consumer) {
        Promise<Void> all = Promise.promise();
        List<Future> futures = new ArrayList<>();

        map.values().forEach((handler) -> {
            Promise<Void> promise = Promise.promise();
            consumer.accept(handler, promise);
            futures.add(promise.future());
        });

        CompositeFuture.all(futures).onComplete(done -> {
            if (done.succeeded()) {
                all.complete();
            } else {
                all.fail(done.cause());
            }
        });
        return all.future();
    }

    @Override
    public void stop(Promise<Void> stop) {
        forAll(CoreDeployment::stop).onComplete(stop);
    }

    @Override
    public void handle(Request request) {
        if (map.containsKey(request.target())) {
            map.get(request.target()).handle(request);
        } else {
            CoreRuntimeException exception = new HandlerMissingException(request.target());
            request.error(exception);
            core.logger(getClass()).onError(exception);
        }
    }

    @Override
    public String address() {
        if (address == null) {
            return toString();
        } else {
            return address;
        }
    }

    @Override
    public String toString() {
        return map.values().stream().map(CoreHandler::address)
                .collect(Collectors.joining(","));
    }
}
