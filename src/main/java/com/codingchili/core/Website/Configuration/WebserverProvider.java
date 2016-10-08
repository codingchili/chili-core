package com.codingchili.core.Website.Configuration;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class WebserverProvider implements Provider {
    private WebserverSettings settings;
    private Vertx vertx;

    public WebserverProvider(Vertx vertx) {
        this.vertx = vertx;
        this.settings = FileConfiguration.instance().getWebsiteSettings();
    }

    public WebserverSettings getSettings() {
        return settings;
    }

    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, settings.getLogserver());
    }
}
