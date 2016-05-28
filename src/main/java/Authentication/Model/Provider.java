package Authentication.Model;

import Authentication.Controller.ClientRequest;
import Authentication.Controller.PacketHandler;
import Authentication.Controller.Protocol;
import Authentication.Controller.RealmRequest;
import Configuration.AuthServerSettings;
import Configuration.ConfigurationLoader;
import Utilities.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public interface Provider {
    AsyncAccountStore getAccountStore();

    Protocol<PacketHandler<ClientRequest>> clientProtocol();

    Protocol<PacketHandler<RealmRequest>> realmProtocol();

    Logger getLogger();

    ConfigurationLoader getConfig();

    AuthServerSettings getAuthserverSettings();

    Vertx getVertx();
}
