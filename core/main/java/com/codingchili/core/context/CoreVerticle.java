package com.codingchili.core.context;

import io.vertx.core.*;

import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;

/**
 * A wrapper for the Vertx api to a deployable service/handler/listener in chili-core.
 * Avoids having to deal with vertx specifics where it is not required.
 */
class CoreVerticle implements Verticle, CoreDeployment {
    private Vertx vertx;
    private CoreContext core;
    private CoreDeployment deployment;
    private Logger logger;

    public CoreVerticle(CoreDeployment deployment, CoreContext core) {
        this.deployment = deployment;
        this.core = core;
        this.logger = core.logger(core.getClass());
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context vcon) {
        this.vertx = vertx;
        this.deployment.init(core);
    }

    @Override
    public void start(Promise<Void> start) {
        Promise<Void> promise = Promise.promise();

        promise.future().onComplete(done -> {
            if (done.succeeded()) {
                if (deployment instanceof CoreService) {
                    logger.onServiceStarted((CoreService) deployment);
                } else if (deployment instanceof CoreListener) {
                    logger.onListenerStarted((CoreListener) deployment);
                }
                start.complete();
            } else {
                start.fail(done.cause());
            }
        });

        deployment.start(promise);
    }

    @Override
    public void stop(Promise<Void> stop) {
        Promise<Void> promise = Promise.promise();

        promise.future().onComplete(done -> {
            if (done.succeeded()) {
                if (deployment instanceof CoreService) {
                    logger.onServiceStopped((CoreService) deployment);
                } else if (deployment instanceof CoreListener) {
                    logger.onListenerStopped((CoreListener) deployment);
                }
                stop.complete();
            } else {
                stop.fail(done.cause());
            }
        });

        deployment.stop(promise);
    }
}
