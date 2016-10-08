package com.codingchili.core.Patching.Configuration;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Patching.Model.PatchKeeper;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class PatchProvider implements Provider {
    private PatchServerSettings settings;
    private Vertx vertx;

    public PatchProvider(Vertx vertx) {
        this.vertx = vertx;
        this.settings = FileConfiguration.instance().getPatchServerSettings();
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, settings.getLogserver());
    }

    public PatchServerSettings getSettings() {
        return settings;
    }

    public PatchKeeper getPatchKeeper() {
        return PatchKeeper.instance(vertx, getLogger(), settings.getPatch());
    }
}
