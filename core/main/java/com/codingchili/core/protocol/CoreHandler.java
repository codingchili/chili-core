package com.codingchili.core.protocol;

import io.vertx.core.*;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.exception.HandlerMissingException;

/**
 * @author Robin Duda
 */
public interface CoreHandler extends Verticle {

    /**
     * Handles an incoming request without exception handling.
     *
     * @param request the request to be handled.
     * @throws CoreException on unhandled error.
     */
    void handle(Request request) throws CoreException;

    /**
     * Get the address of which the handler is providing handlers for.
     *
     * @return the address as a string representation.
     */
    default String address() {
        return context().service().node();
    }

    /**
     *
     * @return
     */
    default Logger logger() {
        return context().logger();
    }

    /**
     *
     * @return
     */
    ServiceContext context();

    default Vertx getVertx() {
        return context().vertx();
    }

    /**
     * Called when the handler is stopped.
     *
     * @param future complete when cleanup is completed.
     */
    default void stop(Future<Void> future) {
        logger().onServerStopped(future);
    }

    /**
     * Called when the handler is started.
     *
     * @param future complete when start initialization is done.
     */
    default void start(Future<Void> future) {
        logger().onServerStarted(future);
    }

    /**
     * @param request
     */
    default void process(Request request) {
        try {
            handle(request);
        } catch (HandlerMissingException e) {
            request.error(e);
            logger().onHandlerMissing(request.route());
        } catch (CoreException e) {
            request.error(e);
        }
    }
}
