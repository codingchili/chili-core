package Configuration;

import Authentication.Configuration.AuthServerSettings;
import Logging.Configuration.ElasticSettings;
import Realm.Configuration.RealmServerSettings;
import Realm.Configuration.RealmSettings;
import Logging.Configuration.LogServerSettings;
import Patching.Configuration.PatchServerSettings;
import Realm.Model.PlayerClass;
import Protocols.Util.Token;
import Protocols.Util.TokenFactory;
import Routing.Configuration.RoutingSettings;
import Website.Configuration.WebserverSettings;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Removes the dependency on valid configurations.
 */
public class ConfigMock implements ConfigurationLoader {

    @Override
    public VertxSettings getVertxSettings() {
        return new VertxSettings();
    }

    @Override
    public PatchServerSettings getPatchServerSettings() {
        return new PatchServerSettings();
    }

    @Override
    public RealmServerSettings getGameServerSettings() {
        return new RealmServerSettings();
    }

    @Override
    public LogServerSettings getLogSettings() {
        return new LogServerSettings();
    }

    @Override
    public AuthServerSettings getAuthSettings() {
        return new AuthServerSettingsMock();
    }

    @Override
    public WebserverSettings getWebsiteSettings() {
        return new WebserverSettings();
    }

    @Override
    public RoutingSettings getRoutingSettings() {
        return new RoutingSettings();
    }

    public RealmSettings getRealm() {
        return new RealmSettingsMock();
    }

    public static class AuthServerSettingsMock extends AuthServerSettings {

        @Override
        public byte[] getClientSecret() {
            return "client.secret".getBytes();
        }

        @Override
        public byte[] getRealmSecret() {
            return "realmName.secret".getBytes();
        }

        @Override
        public boolean isTrustedRealm(String realm) {
            return realm.equals("trusted");
        }
    }

    public static class RealmSettingsMock extends RealmSettings {

        public RealmSettingsMock() {
            super();
            this.setName("realmName.name");
            this.setDescription("description");
            this.setType("type.1");
            this.setTrusted(false);
            this.setSecure(false);
            this.setRemote("remote_ip");
            this.setResources("DIR_RESOURCES");

            generateAuthentication();
            generateMockClasses();
        }

        private void generateMockClasses() {
            ArrayList<PlayerClass> list = new ArrayList<>();

            list.add(new PlayerClass().setName("class.name"));

            this.setClasses(list);
        }

        private void generateAuthentication() {
            this.setAuthentication(new RemoteAuthentication()
                    .setToken(
                            new Token(
                                    new TokenFactory(new AuthServerSettingsMock().getRealmSecret()),
                                    this.getName())));
        }
    }

    public static class RealmServerSettingsMock extends RealmServerSettings {

        @Override
        public ArrayList<RealmSettings> getRealms() {
            ArrayList<RealmSettings> realms = new ArrayList<>();

            realms.add(new RealmSettings());

            return realms;
        }
    }

    public static class LogServerSettingsMock extends LogServerSettings {

        @Override
        public byte[] getSecret() {
            return new byte[0];
        }

        @Override
        public Boolean getConsole() {
            return false;
        }

        @Override
        public ElasticSettings getElastic() {
            return new ElasticSettings().setEnabled(false);
        }

    }
}
