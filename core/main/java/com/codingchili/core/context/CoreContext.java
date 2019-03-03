package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;

import java.util.function.Supplier;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;

/**
 * Provides a simplified subset of the vertx toolkit with some wrapping
 * to simplify usage.
 */
public interface CoreContext {
    /**
     * @return the vertx instance.
     */
    Vertx vertx();

    /**
     * @return get the EventBus attached to the vertx instance.
     */
    EventBus bus();

    /**
     * @return get filesystem access.
     */
    default FileSystem fileSystem() {
        return vertx().fileSystem();
    }

    /**
     * @return the system configuration.
     */
    SystemSettings system();

    /**
     * Set a periodic timer that uses a timersource to adjust interval during runtime.
     *
     * @param delay   a timersource that may change STARTUP_DELAY during runtime.
     * @param name    the name of the periodic timer to log changes to STARTUP_DELAY configuration.
     * @param handler the handler to be invoked when each interval ends.
     */
    void periodic(TimerSource delay, String name, Handler<Long> handler);

    /**
     * Sets a timer that executes the given handler after the given time.
     *
     * @param ms      time unit in ms.
     * @param handler the handler to be invoken when the timer runs out.
     * @return the id of the created timer so that it may be cancelled.
     */
    long timer(long ms, Handler<Long> handler);

    /**
     * Cancels a running timer.
     *
     * @param timer the id of the timer to be stopped.
     */
    void cancel(long timer);

    /**
     * Instantiates an object of the given class identifier and determines the type
     * that should be deployed. May be a {@link CoreHandler} {@link CoreService}
     * {@link CoreListener} or a plain {@link Verticle}.
     *
     * @param target the target class to deploy.
     * @return future completed on deployment.
     */
    Future<String> deploy(String target);

    /**
     * Deploys a new handler in the cluster from given handler with a completion handler.
     *
     * @param handler the handler to be used to handle incoming requests.
     * @return a future to be completed on deployment completion.
     */
    Future<String> handler(Supplier<CoreHandler> handler);

    /**
     * Deploys the given verticle with a completion handler.
     *
     * @param listener the verticle to be deployed.
     * @return future completed on deployment completion.
     */
    Future<String> listener(Supplier<CoreListener> listener);

    /**
     * Deploys the given verticle with a completion handler.
     *
     * @param service the verticle to be deployed.
     * @return future to be completed on deployment completion.
     */
    Future<String> service(Supplier<CoreService> service);

    /**
     * Undeploys a deployed handler by the deployment id.
     *
     * @param deploymentId the id of the deployment to undeploy.
     */
    void stop(String deploymentId);

    /**
     * Call to execute the given blocking handler on a worker thread that is
     * scoped to the current context.
     *
     * @param blocking a method that is blocking, to be executed on worker thread.
     * @param result   handler for the result of the blocking execution.
     * @param <T>      type parameter.
     */
    <T> void blocking(Handler<Future<T>> blocking, Handler<AsyncResult<T>> result);

    /**
     * Call to execute the given blocking handler on a worker thread that is
     * scoped to the current context.
     *
     * @param <T>      type parameter for the result
     * @param blocking a handler that executes blocking code
     * @param ordered  if true, indicates that the tasks must be completed in the same order as they are started.
     * @param result   handler for the result that is called asynchronously
     */
    <T> void blocking(Handler<Future<T>> blocking, boolean ordered, Handler<AsyncResult<T>> result);

    /**
     * @param aClass added as metadata to all logged events.
     * @return get a new instance of a logger.
     */
    Logger logger(Class aClass);

    /**
     * Shuts down the context and underlying pools and connections.
     */
    void close();

    /**
     * @param handler called when the context has closed or failed closing.
     */
    void close(Handler<AsyncResult<Void>> handler);
}
