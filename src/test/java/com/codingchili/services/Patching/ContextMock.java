package com.codingchili.services.Patching;

import io.vertx.core.Vertx;

import com.codingchili.services.Patching.Configuration.PatchContext;

/**
 * @author Robin Duda
 */
class ContextMock extends PatchContext {

    public ContextMock(Vertx vertx) {
        super(vertx);
    }
}
