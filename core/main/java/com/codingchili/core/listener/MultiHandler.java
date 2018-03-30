package com.codingchili.core.listener;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

/**
 * @author Robin Duda
 * <p>
 * <p>
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
     * @param handlers @see #MultiHandler(CoreHandler...)
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
        Future<Void> future = Future.future();

        // if already started - start up the handler.
        if (started.get()) {
            if (map.containsKey(handler.address())) {
                throw new CoreRuntimeException("A deployed handler already exists with address: " + handler.address());
            } else {
                handler.init(core);
                handler.start(future);
            }
        }
        map.put(handler.address(), handler);
        future.complete();
        return future;
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
        Future<Void> future = Future.future();

        if (started.get()) {
            // we are started and the handler exists.
            if (map.containsKey(address)) {
                map.get(address).stop(future);
                map.remove(address);
                return future;
            }
        }
        future.complete();
        return future;
    }

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Future<Void> start) {
        started.getAndSet(true);
        forAll((handler, future) -> {
           handler.init(core);
           handler.start(future);
        }).setHandler(start);
    }

    private Future<Void> forAll(BiConsumer<CoreHandler, Future<Void>> consumer) {
        Future<Void> all = Future.future();
        List<Future> futures = new ArrayList<>();

        map.values().forEach((handler) -> {
            Future<Void> future = Future.future();
            consumer.accept(handler, future);
            futures.add(future);
        });

        CompositeFuture.all(futures).setHandler(done -> {
            if (done.succeeded()) {
                all.complete();
            } else {
                all.fail(done.cause());
            }
        });
        return all;
    }

    @Override
    public void stop(Future<Void> stop) {
        forAll(CoreDeployment::stop).setHandler(stop);
    }

    @Override
    public void handle(Request request) {
        if (map.containsKey(request.target())) {
            map.get(request.target()).handle(request);
        } else {
            throw new HandlerMissingException(request.target());
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
