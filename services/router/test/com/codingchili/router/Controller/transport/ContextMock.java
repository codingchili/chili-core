package com.codingchili.router.controller.transport;

import io.vertx.core.Vertx;

import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.configuration.RouterSettings;

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
