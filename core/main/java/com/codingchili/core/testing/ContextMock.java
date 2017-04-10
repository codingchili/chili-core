package com.codingchili.core.testing;

import com.codingchili.core.configuration.*;
import com.codingchili.core.context.*;
import com.codingchili.core.security.*;

import io.vertx.core.*;

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
    public ServiceConfigurable service() {
        return new ServiceConfigurable() {
        };
    }

    @Override
    public String address() {
        return "";
    }

    @Override
    public RemoteIdentity identity() {
        return new RemoteIdentity("mock.node");
    }
}
