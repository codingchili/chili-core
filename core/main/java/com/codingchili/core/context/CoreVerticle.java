package com.codingchili.core.context;

import io.vertx.core.*;

import com.codingchili.core.listener.CoreLifecycle;

/**
 * @author Robin Duda
 *         <p>
 *         A wrapper for the Vertx api to a deployable service/handler/listener in chili-core.
 *         Avoids having to deal with vertx specifics where it is not required.
 */
class CoreVerticle implements Verticle {
    private Vertx vertx;
    private CoreContext core;
    private CoreLifecycle lifecycle;

    public CoreVerticle(CoreLifecycle lifecycle, CoreContext core) {
        this.lifecycle = lifecycle;
        this.core = core;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context vcon) {
        this.vertx = vertx;
        lifecycle.init(core);
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        lifecycle.start(start);
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        lifecycle.stop(stop);
    }
}
