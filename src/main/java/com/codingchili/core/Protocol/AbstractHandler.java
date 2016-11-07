package com.codingchili.core.Protocol;

import io.vertx.core.Future;

import com.codingchili.core.Context.ServiceContext;
import com.codingchili.core.Exception.*;
import com.codingchili.core.Logging.Logger;

/**
 * @author Robin Duda
 */
public abstract class AbstractHandler<T extends ServiceContext> {
    private final Logger logger;
    protected final T context;
    private final String address;

    protected AbstractHandler(T context, String address) {
        this.context = context;
        this.address = address;
        this.logger = context.logger();
    }

    public void process(Request request) {
        try {
            handle(request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized(e);
        } catch (HandlerMissingException e) {
            request.error(e);
            logger.onHandlerMissing(request.action());
        } catch (CoreException e) {
            request.error(e);
        }
    }

    public abstract void handle(Request request) throws CoreException;

    public void stop(Future<Void> future) {
        logger.onServerStopped(future);
    }

    public void start(Future<Void> future) {
        logger.onServerStarted(future);
    }

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
