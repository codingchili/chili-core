package Configuration;

import Authentication.Configuration.AuthServerSettings;
import Realm.Configuration.RealmServerSettings;
import Realm.Configuration.RealmSettings;
import Logging.Configuration.LogServerSettings;
import Patching.Configuration.PatchServerSettings;
import Realm.Model.PlayerClass;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Website.Configuration.WebserverSettings;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Removes the dependency on valid configurations.
 */
public class ConfigMock implements ConfigurationLoader {

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
        public String getDescription() {
            return "description";
        }

        @Override
        public Boolean getTrusted() {
            return false;
        }

        @Override
        public Boolean getSecure() {
            return false;
        }

        @Override
        public String getType() {
            return "type";
        }

        @Override
        public String getResources() {
            return "DIR_RESOURCES";
        }

        @Override
        public String getRemote() {
            return "localhost";
        }

        @Override
        public int getPort() {
            return 11132;
        }

        @Override
        public int getProxy() {
            return 11132;
        }

        @Override
        public ArrayList<PlayerClass> getClasses() {
            ArrayList<PlayerClass> list = new ArrayList<>();

            list.add(new PlayerClass().setName("class.name"));

            return list;
        }

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

    public static class RealmServerSettingsMock extends RealmServerSettings {

        @Override
        public ArrayList<RealmSettings> getRealms() {
            ArrayList<RealmSettings> realms = new ArrayList<>();

            realms.add(new RealmSettings());

            return realms;
        }
    }
}
