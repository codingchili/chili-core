package com.codingchili.patching;

import com.codingchili.patching.configuration.PatchContext;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
class ContextMock extends PatchContext {

    public ContextMock(Vertx vertx) {
        super(vertx);
    }
}
