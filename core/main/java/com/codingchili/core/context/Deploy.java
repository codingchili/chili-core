package com.codingchili.core.context;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import com.codingchili.core.protocol.CoreHandler;

/**
 * @author Robin Duda
 *
 * Provides a shorthand for deploying a created handler.
 */
public abstract class Deploy {

    /**
     * Deploys the given handler with its contained context.
     * @param handler the handler to be deployed.
     */
    public static void service(CoreHandler handler) {
        handler.context().deploy(handler);
    }

    /**
     * Deploys the given handler with its contained context.
     * @param handler the handler to be deployed.
     * @param result the handler to call when deployment is completed.
     */
    public static void service(CoreHandler handler, Handler<AsyncResult<String>> result) {
        handler.context().deploy(handler, result);
    }
}
