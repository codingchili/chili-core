package com.codingchili.core.protocol;

import com.codingchili.core.context.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.exception.HandlerMissingException;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         <p>
 *         A simplified handler that may be deployed directly.
 */
public interface CoreHandler extends Verticle {

    /**
     * init method called synchronously before the service is started.
     *
     * @param core core context that the service was deployed from.
     */
    void init(CoreContext core);

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
     * @return a logger attached to the service context.
     */
    default Logger logger() {
        return context().logger();
    }

    /**
     * @return the context of the service.
     */
    ServiceContext context();

    /**
     * @return returns the vertx instance in the services context.
     */
    default Vertx getVertx() {
        return context().vertx();
    }

    /**
     * Wrapped init method to provide an improved init.
     *
     * @param vertx   the vertx context that deployed this verticle.
     * @param context the vertx context.
     */
    default void init(Vertx vertx, Context context) {
        init(new SystemContext(vertx));
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
     * Processes a request using the default request exception handling.
     *
     * @param request the request to process.
     */
    default void process(Request request) {
        try {
            handle(request);
        } catch (HandlerMissingException e) {
            request.error(e);
            logger().onHandlerMissing(request.route());
        } catch (CoreException | CoreRuntimeException e) {
            request.error(e);
        }
    }
}
