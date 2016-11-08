package com.codingchili.core.Context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;

import com.codingchili.core.Configuration.System.SystemSettings;
import com.codingchili.core.Logging.Logger;
import com.codingchili.core.Protocol.AbstractHandler;
import com.codingchili.core.Security.RemoteIdentity;

/**
 * @author Robin Duda
 */
public interface CoreContext {
    Vertx vertx();

    EventBus bus();

    SystemSettings system();

    void periodic(TimerSource delay, String name, Handler<Long> handler);

    long timer(long ms, Handler<Long> handler);

    void deploy(AbstractHandler handler);

    void deploy(Verticle verticle);

    void deploy(String verticle, Handler<AsyncResult<String>> result);

    void deploy(AbstractHandler handler, Handler<AsyncResult<String>> result);

    void deploy(Verticle verticle, Handler<AsyncResult<String>> result);

    RemoteIdentity identity();

    String handler();

    Logger console();
}
