package com.codingchili.core.context;

import com.codingchili.core.listener.CoreDeployment;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.logging.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 * <p>
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
        this.logger = core.logger();
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
    public void start(Future<Void> start) {
        Future<Void> future = Future.future();

        future.setHandler(done -> {
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

        deployment.start(future);
    }

    @Override
    public void stop(Future<Void> stop) {
        Future<Void> future = Future.future();

        future.setHandler(done -> {
            if (done.succeeded()) {
                if (deployment instanceof CoreService) {
                    logger.onServiceStopped(stop, (CoreService) deployment);
                } else if (deployment instanceof CoreListener) {
                    logger.onListenerStopped((CoreListener) deployment);
                    stop.complete();
                }
            } else {
                stop.fail(done.cause());
            }
        });

        deployment.stop(future);
    }
}
