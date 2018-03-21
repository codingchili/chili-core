package com.codingchili.website.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.Logger;

import java.nio.file.Paths;

import static com.codingchili.common.Strings.*;
import static com.codingchili.website.configuration.WebserverSettings.PATH_WEBSERVER;

/**
 * @author Robin Duda
 * <p>
 * Context for the web server.
 */
public class WebserverContext extends SystemContext implements ServiceContext {
    private Logger logger;

    public WebserverContext(CoreContext core) {
        super(core);
        this.logger = logger(getClass());
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
        logger.event(LOG_PAGE_LOAD).put(LOG_AGENT, request.data().getString(LOG_USER_AGENT)).send();
    }
}
