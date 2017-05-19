package com.codingchili.core.listener.transport;

import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.configuration.RouterSettings;

import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class ContextMock extends RouterContext {
    private RouterSettings settings;


    public ContextMock(Vertx vertx) {
        super(vertx);
    }

    public ContextMock setSettings(RouterSettings settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public RouterSettings service() {
        return settings;
    }
}
