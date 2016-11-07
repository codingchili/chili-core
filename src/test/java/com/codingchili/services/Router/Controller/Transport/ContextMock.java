package com.codingchili.services.Router.Controller.Transport;

import io.vertx.core.Vertx;

import com.codingchili.services.Router.Configuration.RouterContext;
import com.codingchili.services.Router.Configuration.RouterSettings;

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
