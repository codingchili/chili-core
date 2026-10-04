package com.codingchili.core.context;

import io.vertx.core.*;

import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;

/**
 * A wrapper for the Vertx api to a deployable service/handler/listener in chili-core.
 * Avoids having to deal with vertx specifics where it is not required.
 */
class CoreVerticle extends VerticleBase {
    private final CoreContext core;
    private final CoreDeployment deployment;
    private final Logger logger;

    public CoreVerticle(CoreDeployment deployment, CoreContext core) {
        this.deployment = deployment;
        this.core = core;
        this.logger = core.logger(core.getClass());
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.deployment.init(core);
    }

    @Override
    public Future<?> start() {
        Promise<Void> promise = Promise.promise();
        deployment.start(promise);

        return promise.future().onSuccess(done -> {
            if (deployment instanceof CoreService) {
                logger.onServiceStarted((CoreService) deployment);
            } else if (deployment instanceof CoreListener) {
                logger.onListenerStarted((CoreListener) deployment);
            }
        });
    }

    @Override
    public Future<?> stop() {
        Promise<Void> promise = Promise.promise();
        deployment.stop(promise);

        return promise.future().onSuccess(done -> {
            if (deployment instanceof CoreService) {
                logger.onServiceStopped((CoreService) deployment);
            } else if (deployment instanceof CoreListener) {
                logger.onListenerStopped((CoreListener) deployment);
            }
        });
    }
}
