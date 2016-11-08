package com.codingchili.services.Website.Configuration;

import io.vertx.core.Vertx;

import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Protocol.Request;

import com.codingchili.core.Context.ServiceContext;

import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.services.Website.Configuration.WebserverSettings.PATH_WEBSERVER;

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

    public void onPageLoaded(Request request) {
        log(event(LOG_PAGE_LOAD)
                .put(LOG_AGENT, request.data().getString(LOG_USER_AGENT)));
    }
}
