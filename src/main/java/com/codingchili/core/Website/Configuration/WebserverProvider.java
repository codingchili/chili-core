package com.codingchili.core.Website.Configuration;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Vertx;

import static com.codingchili.core.Configuration.Strings.PATH_WEBSERVER;

/**
 * @author Robin Duda
 */
public class WebserverProvider implements Provider {
    private final WebserverSettings settings;
    private final Vertx vertx;

    public WebserverProvider(Vertx vertx) {
        this.vertx = vertx;
        this.settings = FileConfiguration.get(PATH_WEBSERVER, WebserverSettings.class);
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
