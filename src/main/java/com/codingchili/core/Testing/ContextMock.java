package com.codingchili.core.Testing;

import io.vertx.core.Vertx;

import com.codingchili.core.Configuration.ServiceConfigurable;
import com.codingchili.core.Context.*;
import com.codingchili.core.Security.RemoteIdentity;

/**
 * @author Robin Duda
 */
public class ContextMock extends ServiceContext implements CoreContext {
    public ContextMock(Vertx vertx) {
        super(vertx);
    }

    public ContextMock(CoreContext context) {
        super(context);
    }

    @Override
    protected ServiceConfigurable service() {
        return new ServiceConfigurable() {};
    }

    @Override
    public RemoteIdentity identity() {
        return new RemoteIdentity("mock.node", "localhost");
    }
}
