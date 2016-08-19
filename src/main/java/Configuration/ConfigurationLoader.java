package Configuration;

import Authentication.Configuration.AuthServerSettings;
import Realm.Configuration.GameServerSettings;
import Logging.Configuration.LogServerSettings;
import Patching.Configuration.PatchServerSettings;

/**
 * @author Robin Duda
 */
public interface ConfigurationLoader {
    PatchServerSettings getPatchServerSettings();

    GameServerSettings getGameServerSettings();

    LogServerSettings getLogSettings();

    AuthServerSettings getAuthSettings();

    class Address {
        public final static String LOGS = Strings.LOG_ID;
    }
}
