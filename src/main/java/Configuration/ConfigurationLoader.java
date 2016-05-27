package Configuration;

/**
 * @author Robin Duda
 */
public interface ConfigurationLoader {
    WebServerSettings getWebServerSettings();

    GameServerSettings getGameServerSettings();

    LogServerSettings getLogSettings();

    AuthServerSettings getAuthSettings();

    class Address {
        public final static String LOGS = "LOGGING";
    }
}
