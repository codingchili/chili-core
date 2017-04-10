package com.codingchili.core.testing;

import com.codingchili.core.configuration.*;
import com.codingchili.core.context.*;

import io.vertx.core.*;

/**
 * @author Robin Duda
 */
public class ContextMock extends ServiceContext implements CoreContext {
    public static final String NODE = "name.node";

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
