package Patching.Configuration;

import Configuration.FileConfiguration;
import Configuration.Provider;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Patching.Controller.ClientRequest;
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
