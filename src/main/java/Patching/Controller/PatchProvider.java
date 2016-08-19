package Patching.Controller;

import Configuration.ConfigurationLoader;
import Configuration.FileConfiguration;
import Configuration.Provider;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Patching.Configuration.PatchServerSettings;
import Patching.Model.PatchKeeper;
import Protocols.AuthorizationHandler.Access;
import Protocols.PacketHandler;
import Protocols.Protocol;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class PatchProvider implements Provider {
    private PatchServerSettings settings;
    private ConfigurationLoader loader;
    private Protocol<PacketHandler<ClientRequest>> protocol = new Protocol<>(Access.PUBLIC);
    private Vertx vertx;

    public PatchProvider(Vertx vertx) {
        this.vertx = vertx;
            this.loader = FileConfiguration.instance();
            this.settings = loader.getPatchServerSettings();
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

    public PatchServerSettings getSettings() {
        return settings;
    }

    public Protocol<PacketHandler<ClientRequest>> protocol() {
        return protocol;
    }

    PatchKeeper getPatchKeeper() {
        return PatchKeeper.instance(vertx, getLogger(), settings.getPatch());
    }
}
