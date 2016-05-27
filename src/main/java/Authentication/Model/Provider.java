package Authentication.Model;

import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Controller.ClientProtocol;
import Authentication.Controller.RealmProtocol;
import Configuration.AuthServerSettings;
import Configuration.ConfigurationLoader;
import Utilities.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public interface Provider {
    AsyncAccountStore getAccountStore();

    ClientProtocol clientProtocol(Access access);

    RealmProtocol realmProtocol(Access access);

    Logger getLogger();

    ConfigurationLoader getConfig();

    AuthServerSettings getAuthserverSettings();

    Vertx getVertx();
}
