package com.codingchili.core.Testing;

import io.vertx.core.Vertx;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.SystemContext;
import com.codingchili.core.Security.RemoteIdentity;

/**
 * @author Robin Duda
 */
public class ContextMock extends SystemContext implements CoreContext {
    public ContextMock(Vertx vertx) {
        super(vertx);
    }

    @Override
    public RemoteIdentity identity() {
        return new RemoteIdentity("mock.node", "localhost");
    }
}
