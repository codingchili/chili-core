package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.AbstractHandler;
import com.codingchili.core.security.RemoteIdentity;

/**
 * @author Robin Duda
 *
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
     * @return the system configuration.
     */
    SystemSettings system();

    /**
     * Set a periodic timer that uses a timersource to adjust interval during runtime.
     * @param delay a timersource that may change delay during runtime.
     * @param name the name of the periodic timer to log changes to delay configuration.
     * @param handler the handler to be invoked when each interval ends.
     */
    void periodic(TimerSource delay, String name, Handler<Long> handler);

    /**
     * Sets a timer that executes the given handler after the given time.
     * @param ms time unit in ms.
     * @param handler the handler to be invoken when the timer runs out.
     * @return the id of the created timer so that it may be cancelled.
     */
    long timer(long ms, Handler<Long> handler);

    /**
     * Cancels a running timer.
     * @param timer the id of the timer to be stopped.
     */
    void cancel(long timer);

    /**
     * Deploy a new handler in the cluster from given handler.
     * @param handler the handler to be used to handle incoming requests.
     */
    void deploy(AbstractHandler handler);

    /**
     * @param verticle deploys the given verticle.
     */
    void deploy(Verticle verticle);

    /**
     * Deploys a given verticle by name with a completion handler.
     * @param verticle the name of the verticle to deploy.
     * @param result the handler to be invoked on deployment completion.
     */
    void deploy(String verticle, Handler<AsyncResult<String>> result);

    /**
     * Deploys a new handler in the cluster from given handler with a completion handler.
     * @param handler the handler to be used to handle incoming requests.
     * @param result the handler to be invoked on deployment completion.
     */
    void deploy(AbstractHandler handler, Handler<AsyncResult<String>> result);

    /**
     * Deploys the given verticle with a completion handler.
     * @param verticle the verticle to be deployed.
     * @param result the handler to be invoked on deployment completion.
     */
    void deploy(Verticle verticle, Handler<AsyncResult<String>> result);

    /**
     * @return the identity of the service or system component owning the context.
     */
    RemoteIdentity identity();

    /**
     * @return returns the name of the context scope.
     */
    String handler();

    /**
     * @return get the console logger which writes to std out.
     */
    Logger console();
}
