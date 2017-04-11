package com.codingchili.core.protocol;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.ServiceContext;

/**
 * @author Robin Duda
 *         <p>
 *         Base handler for processing incoming messages.
 */
public abstract class AbstractHandler<T extends ServiceContext> implements CoreHandler {
    protected final T context;
    protected String address;

    /**
     * @param context the context to attach to the handler.
     * @param address the address the handler is listenening to.
     */
    protected AbstractHandler(T context, String address) {
        this.context = context;
        this.address = address;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public abstract void handle(Request request) throws CoreException;

    @Override
    public T context() {
        return context;
    }
}
