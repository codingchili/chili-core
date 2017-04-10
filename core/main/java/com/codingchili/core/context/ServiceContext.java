package com.codingchili.core.context;

import com.codingchili.core.configuration.*;
import com.codingchili.core.logging.*;

import io.vertx.core.*;
import io.vertx.core.json.*;

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
