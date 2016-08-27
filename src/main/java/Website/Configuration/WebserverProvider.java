package Website.Configuration;

import Configuration.ConfigurationLoader;
import Configuration.FileConfiguration;
import Configuration.Provider;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class WebserverProvider implements Provider {
    private ConfigurationLoader loader;
    private WebserverSettings settings;
    private Vertx vertx;

    public WebserverProvider(Vertx vertx) {
        this.vertx = vertx;
        this.loader = FileConfiguration.instance();
        this.settings = loader.getWebsiteSettings();
    }

    public WebserverSettings getSettings() {
        return settings;
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public ConfigurationLoader getConfig() {
        return loader;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}
