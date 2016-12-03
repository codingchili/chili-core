package com.codingchili.patching;

import io.vertx.core.Vertx;

import com.codingchili.patching.configuration.PatchContext;

/**
 * @author Robin Duda
 */
class ContextMock extends PatchContext {

    public ContextMock(Vertx vertx) {
        super(vertx);
    }
}
