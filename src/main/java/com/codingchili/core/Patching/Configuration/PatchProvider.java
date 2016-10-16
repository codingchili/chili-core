package com.codingchili.core.Patching.Configuration;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Patching.Model.PatchKeeper;
import com.codingchili.core.Patching.Model.PatchListener;
import io.vertx.core.Vertx;

import static com.codingchili.core.Configuration.Strings.ID_PATCHER;
import static com.codingchili.core.Configuration.Strings.PATH_PATCHSERVER;

/**
 * @author Robin Duda
 */
public class PatchProvider implements Provider {
    private final PatchServerSettings settings;
    private final DefaultLogger logger;
    private final Vertx vertx;

    public PatchProvider(Vertx vertx) {
        this.vertx = vertx;
        this.settings = FileConfiguration.get(PATH_PATCHSERVER, PatchServerSettings.class);
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public PatchServerSettings getSettings() {
        return settings;
    }

    public PatchKeeper getPatchKeeper() {
        return PatchKeeper.instance(vertx, new PatchListener() {
            @Override
            public void onPatchLoaded(String name, String version) {
                logger.onPatchLoaded(name, version);
            }

            @Override
            public void onPatchReloading(String name, String version) {
                logger.onPatchReloading(name, version);
            }

            @Override
            public void onPatchReloaded(String name, String version) {
                logger.onPatchReloaded(name, version);
            }

            @Override
            public void onFileLoaded(String path) {
                logger.onFileLoaded(ID_PATCHER, path);
            }
        });
    }
}
