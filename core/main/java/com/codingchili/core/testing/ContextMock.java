package com.codingchili.core.testing;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;

import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class ContextMock extends ServiceContext implements CoreContext {
    public static final String NODE = "handler.node";

    public ContextMock(Vertx vertx) {
        super(vertx);
    }

    public ContextMock(CoreContext context) {
        super(context);
    }

    @Override
    public ServiceConfigurable service() {
        return new ServiceConfigurable() {
        };
    }

    @Override
    public String node() {
        return NODE;
    }
}
