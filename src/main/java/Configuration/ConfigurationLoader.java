package Configuration;

import Configuration.Authserver.AuthServerSettings;
import Configuration.Gameserver.GameServerSettings;
import Configuration.Logserver.LogServerSettings;
import Configuration.MetaServer.MetaServerSettings;

/**
 * @author Robin Duda
 */
public interface ConfigurationLoader {
    MetaServerSettings getMetaServerSettings();

    GameServerSettings getGameServerSettings();

    LogServerSettings getLogSettings();

    AuthServerSettings getAuthSettings();

    class Address {
        public final static String LOGS = "LOGGING";
    }

    String RESOURCES = "resources/";
}
