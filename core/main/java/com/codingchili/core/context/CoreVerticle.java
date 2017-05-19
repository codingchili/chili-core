package com.codingchili.core.context;

import com.codingchili.core.listener.CoreDeployment;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         <p>
 *         A wrapper for the Vertx api to a deployable service/handler/listener in chili-core.
 *         Avoids having to deal with vertx specifics where it is not required.
 */
class CoreVerticle implements Verticle, CoreDeployment {
    private Vertx vertx;
    private CoreContext core;
    private CoreDeployment deployment;

    public CoreVerticle(CoreDeployment deployment, CoreContext core) {
        this.deployment = deployment;
        this.core = core;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context vcon) {
        this.vertx = vertx;
        deployment.init(core);
    }

    @Override
    public void start(Future<Void> start) {
        deployment.start(start);
    }

    @Override
    public void stop(Future<Void> stop) {
        deployment.stop(stop);
    }
}
