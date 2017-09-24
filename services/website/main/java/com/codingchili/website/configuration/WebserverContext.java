package com.codingchili.website.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.Request;

import java.nio.file.Paths;

import static com.codingchili.common.Strings.*;
import static com.codingchili.website.configuration.WebserverSettings.PATH_WEBSERVER;

/**
 * @author Robin Duda
 * <p>
 * Context for the web server.
 */
public class WebserverContext extends ServiceContext {

    public WebserverContext(CoreContext core) {
        super(core);
    }

    public WebserverSettings service() {
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
        return Paths.get(service().getResources()).toString();
    }

    public void onPageLoaded(Request request) {
        log(event(LOG_PAGE_LOAD).put(LOG_AGENT, request.data().getString(LOG_USER_AGENT)));
    }
}
