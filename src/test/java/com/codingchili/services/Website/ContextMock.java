package com.codingchili.services.website;

import io.vertx.core.Vertx;

import com.codingchili.core.configuration.CoreStrings;

import com.codingchili.services.website.configuration.WebserverContext;
import com.codingchili.services.website.configuration.WebserverSettings;

/**
 * @author Robin Duda
 */
class ContextMock extends WebserverContext {
    public ContextMock(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected WebserverSettings service() {
        return new WebserverSettings();
    }

    @Override
    public boolean isGzip() {
        return service().getGzip();
    }

    @Override
    public String getMissingPage() {
        return "/404.json";
    }

    @Override
    public String getStartPage() {
        return "/index.html";
    }

    @Override
    public String resources() {
        return CoreStrings.testDirectory("Services/website");
    }
}
