package com.codingchili.core.listener.transport;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.stream.Stream;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.LOG_AT;

/**
 * Listens for requests addressed to the attached handler and forwards
 * the requests to it.
 */
public class ClusterListener implements CoreListener, DeploymentAware {
    private CoreHandler handler;
    private CoreContext core;
    private Logger logger;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        this.logger = ListenerExceptionLogger.create(core, this, handler);
        handler.init(core);
    }

    @Override
    public CoreListener settings(ListenerSettings settings) {
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Promise<Void> start) {
        Stream.of(handler.address().split(","))
                .forEach(address -> {
                    core.bus().consumer(handler.address())
                            .exceptionHandler(logger::onError)
                            .handler(message -> handler.handle(new ClusterRequest(message)));
                });
        handler.start(start);
    }

    @Override
    public void stop(Promise<Void> stop) {
        handler.stop(stop);
    }

    @Override
    public int instances() {
        return (handler instanceof DeploymentAware) ?
                ((DeploymentAware) handler).instances() : Configurations.system().getHandlers();
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + LOG_AT + handler.address();
    }
}
