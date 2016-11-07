package com.codingchili.core.Context;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import com.codingchili.core.Protocol.AbstractHandler;

/**
 * @author Robin Duda
 *
 * deploytool to deploy to remote and generate configuration files for each host.
 */
public class Deploy {
    public static void service(AbstractHandler handler) {
        handler.context().deploy(handler);
    }

    public static void service(AbstractHandler handler, Handler<AsyncResult<String>> result) {
        handler.context().deploy(handler, result);
    }
}
