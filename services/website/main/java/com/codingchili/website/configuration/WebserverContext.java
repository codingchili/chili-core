package com.codingchili.website.configuration;

import java.nio.file.*;

import com.codingchili.core.context.*;
import com.codingchili.core.files.*;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.website.configuration.WebserverSettings.*;

/**
 * @author Robin Duda
 *
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
