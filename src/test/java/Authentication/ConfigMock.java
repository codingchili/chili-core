package Authentication;

import Configuration.*;
import Configuration.Authserver.AuthServerSettings;
import Configuration.Gameserver.GameServerSettings;
import Configuration.Gameserver.RealmSettings;
import Configuration.Logserver.LogServerSettings;
import Configuration.MetaServer.MetaServerSettings;
import Configuration.Gameserver.Advertise;
import Game.Model.PlayerClass;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Removes the dependency on valid configurations.
 */
public class ConfigMock implements ConfigurationLoader {

    @Override
    public MetaServerSettings getMetaServerSettings() {
        return new MetaServerSettings();
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
            return "RESOURCES";
        }

        @Override
        public String getRemote() {
            return "localhost";
        }

        @Override
        public Advertise getAdvertise() {
            return new Advertise("localhost", 11132);
        }

        @Override
        public int getPort() {
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

    public static class GameSettingsMock extends GameServerSettings {

        @Override
        public ArrayList<RealmSettings> getRealms() {
            ArrayList<RealmSettings> realms = new ArrayList<>();

            realms.add(new RealmSettingsMock());

            return realms;
        }
    }
}
