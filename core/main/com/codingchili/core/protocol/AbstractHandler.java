package com.codingchili.core.protocol;

import io.vertx.core.Future;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

/**
 * @author Robin Duda
 *
 * Base handler for processing incoming messages.
 */
public abstract class AbstractHandler<T extends ServiceContext> {
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

    /**
     * Processes an incoming request.
     *
     * @param request the request to be processed.
     */
    public void process(Request request) {
        try {
            handle(request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized(e);
        } catch (HandlerMissingException e) {
            request.error(e);
            logger.onHandlerMissing(request.route());
        } catch (CoreException e) {
            request.error(e);
        }
    }

    /**
     * Handles an incoming request.
     *
     * @param request the request to be handled.
     * @throws CoreException on unhandled error.
     */
    public abstract void handle(Request request) throws CoreException;

    /**
     * Called when the handler is stopped.
     *
     * @param future complete when cleanup is completed.
     */
    public void stop(Future<Void> future) {
        logger.onServerStopped(future);
    }

    /**
     * Called when the handler is started.
     *
     * @param future complete when start initialization is done.
     */
    public void start(Future<Void> future) {
        logger.onServerStarted(future);
    }

    /**
     * @return the context attached to the handler.
     */
    public T context() {
        return context;
    }

    /**
     * Get the address of which the handler is providing handlers for.
     *
     * @return the address as a string representation.
     */
    String getAddress() {
        return address;
    }
}
