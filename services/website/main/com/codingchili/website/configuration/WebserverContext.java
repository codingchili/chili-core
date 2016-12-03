package com.codingchili.website.configuration;

import io.vertx.core.Vertx;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Request;

import com.codingchili.core.context.ServiceContext;

import static com.codingchili.common.Strings.*;
import static com.codingchili.website.configuration.WebserverSettings.PATH_WEBSERVER;

/**
 * @author Robin Duda
 */
public class WebserverContext extends ServiceContext {

    public WebserverContext(Vertx vertx) {
        super(vertx);
    }

    protected WebserverSettings service() {
        return Configurations.get(PATH_WEBSERVER, WebserverSettings.class);
    }

    public String getStartPage() {
        return service().getStartPage();
    }

    public String getMissingPage() {
        return service().getMissingPage();
    }

    public boolean isGzip() {
        return service().getGzip();
    }

    public String resources() {
        return service().getResources();
    }

    public void onPageLoaded(Request request) {
        log(event(LOG_PAGE_LOAD)
                .put(LOG_AGENT, request.data().getString(LOG_USER_AGENT)));
    }
}