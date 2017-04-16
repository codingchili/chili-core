package com.codingchili.core.context;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.logging.RemoteLogger;

/**
 * @author Robin Duda
 *         <p>
 *         Provides basic context requirements for all service-contexts.
 */
public abstract class ServiceContext extends SystemContext {
    protected Logger logger;

    protected ServiceContext(CoreContext context) {
        this(context.vertx());
    }

    protected ServiceContext(Vertx vertx) {
        super(vertx);
        this.logger = new RemoteLogger(this);
    }

    /**
     * @return the configuration associated with the current context.
     */
    public abstract ServiceConfigurable service();

    @Override
    public String node() {
        return service().node();
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    protected void onMetricsSnapshot(JsonObject json) {
        logger.onMetricsSnapshot(json);
    }

    protected void log(JsonObject json) {
        logger.log(json);
    }
}
