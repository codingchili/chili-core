package Configuration;

import Authentication.Configuration.AuthServerSettings;
import Realm.Configuration.RealmServerSettings;
import Logging.Configuration.LogServerSettings;
import Patching.Configuration.PatchServerSettings;
import Routing.Configuration.RoutingSettings;
import Website.Configuration.WebserverSettings;

/**
 * @author Robin Duda
 */
public interface ConfigurationLoader {
    PatchServerSettings getPatchServerSettings();

    RealmServerSettings getGameServerSettings();

    LogServerSettings getLogSettings();

    AuthServerSettings getAuthSettings();

    WebserverSettings getWebsiteSettings();

    RoutingSettings getRoutingSettings();
}
