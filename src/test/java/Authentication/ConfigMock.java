package Authentication;

import Configuration.*;
import Utilities.Token;
import Utilities.TokenFactory;

/**
 * @author Robin Duda
 *         <p>
 *         Removes the dependency on valid configurations.
 */
public class ConfigMock implements ConfigurationLoader {

    @Override
    public WebServerSettings getWebServerSettings() {
        return new WebServerSettings();
    }

    @Override
    public GameServerSettings getGameServerSettings() {
        return new GameServerSettings();
    }

    @Override
    public LogServerSettings getLogSettings() {
        return new LogServerSettings();
    }

    @Override
    public AuthServerSettings getAuthSettings() {
        return new AuthServerSettingsMock();
    }


    public RealmSettings getRealm() {
        return new RealmSettingsMock();
    }

    public static class AuthServerSettingsMock extends AuthServerSettings {

        @Override
        public Integer getRealmPort() {
            return 12502;
        }

        @Override
        public Integer getClientPort() {
            return 13091;
        }

        @Override
        public byte[] getClientSecret() {
            return "client.secret".getBytes();
        }

        @Override
        public byte[] getRealmSecret() {
            return "realmName.secret".getBytes();
        }

    }

    public static class RealmSettingsMock extends RealmSettings {

        @Override
        public String getName() {
            return "realmName.name";
        }

        @Override
        public RemoteAuthentication getAuthentication() {
            return new RemoteAuthentication()
                    .setToken(
                            new Token(
                                    new TokenFactory(new AuthServerSettingsMock().getRealmSecret()),
                                    this.getName()));
        }
    }
}
