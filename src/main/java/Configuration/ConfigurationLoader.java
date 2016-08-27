package Configuration;

import Authentication.Configuration.AuthServerSettings;
import Realm.Configuration.RealmServerSettings;
import Logging.Configuration.LogServerSettings;
import Patching.Configuration.PatchServerSettings;
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

    class Address {
        public final static String LOGS = Strings.LOG_ID;
    }
}
