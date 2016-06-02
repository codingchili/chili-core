package Meta.Controller;

import Configuration.ConfigurationLoader;
import Configuration.FileConfiguration;
import Configuration.Provider;
import Configuration.MetaServer.MetaServerSettings;
import Meta.Model.PatchKeeper;
import Protocols.AuthorizationHandler.Access;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class MetaProvider implements Provider {
    private MetaServerSettings settings = FileConfiguration.instance().getMetaServerSettings();
    private Protocol<PacketHandler<ClientRequest>> protocol = new Protocol<>(Access.PUBLIC);
    private Vertx vertx;
    private PatchKeeper patchKeeper;

    public MetaProvider(Vertx vertx) {
        this.vertx = vertx;
        this.patchKeeper = PatchKeeper.instance(vertx, getLogger());
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public ConfigurationLoader getConfig() {
        return FileConfiguration.instance();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    public MetaServerSettings getSettings() {
        return settings;
    }

    public Protocol<PacketHandler<ClientRequest>> protocol() {
        return protocol;
    }

    public PatchKeeper getPatchKeeper() {
        return patchKeeper;
    }
}
