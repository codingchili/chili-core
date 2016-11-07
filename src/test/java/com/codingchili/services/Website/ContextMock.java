package com.codingchili.services.Website;

import io.vertx.core.Vertx;

import com.codingchili.services.Website.Configuration.WebserverContext;

/**
 * @author Robin Duda
 */
class ContextMock extends WebserverContext {
    public ContextMock(Vertx vertx) {
        super(vertx);
    }

    @Override
    public boolean isGzip() {
        return false;
    }

    @Override
    public String getMissingPage() {
        return "/bower.json";
    }
}
