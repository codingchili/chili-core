package com.codingchili.core.context;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.logging.*;
import com.codingchili.core.security.RemoteIdentity;

/**
 * @author Robin Duda
 *
 * Provides basic context requirements for all service-contexts.
 */
public abstract class ServiceContext extends SystemContext {
    protected final Logger logger;

    protected ServiceContext(CoreContext context) {
        super(context);
        this.logger = new RemoteLogger(this);
    }

    protected ServiceContext(Vertx vertx) {
        super(vertx);
        this.logger = new RemoteLogger(this);
    }

    /**
     * @return the configuration associated with the current context.
     */
    protected abstract ServiceConfigurable service();

    public Logger logger() {
        return logger;
    }

    @Override
    public RemoteIdentity identity() {
        return new RemoteIdentity(service().node(), service().host());
    }

    @Override
    protected void onMetricsSnapshot(JsonObject json) {
        logger.onMetricsSnapshot(json);
    }

    protected void log(JsonObject json) {
        logger.log(json);
    }

    protected JsonObject event(String event) {
        return event(event, Level.INFO);
    }

    protected JsonObject event(String event, Level level) {
        return logger.event(event, level);
    }
}
