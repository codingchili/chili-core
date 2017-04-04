package com.codingchili.core.protocol;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.logging.Logger;

/**
 * @author Robin Duda
 *         <p>
 *         Base handler for processing incoming messages.
 */
public abstract class AbstractHandler<T extends ServiceContext> implements CoreHandler {
    private final Logger logger;
    protected final T context;
    private final String address;

    /**
     * @param context the context to attach to the handler.
     * @param address the address the handler is listenening to.
     */
    protected AbstractHandler(T context, String address) {
        this.context = context;
        this.address = address;
        this.logger = context.logger();
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public abstract void handle(Request request) throws CoreException;

    @Override
    public String address() {
        return address;
    }

    @Override
    public T context() {
        return context;
    }

    @Override
    public Vertx getVertx() {
        return context.vertx();
    }

    @Override
    public void init(Vertx vertx, Context context) {
    }
}
