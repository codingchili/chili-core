package Configuration.Gameserver;

import Configuration.Configurable;
import Configuration.RemoteAuthentication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains settings for the game server container.
 */
@JsonIgnoreProperties({"realms", "path", "name"})
public class GameServerSettings implements Configurable {
    public static final String GAMESERVER_PATH = "conf/system/gameserver.json";
    public static final String REALM_PATH = "conf/realmName/";
    private RemoteAuthentication logserver;
    private ArrayList<RealmSettings> realms = new ArrayList<>();

    public void setRealms(ArrayList<RealmSettings> realms) {
        this.realms = realms;
    }

    public ArrayList<RealmSettings> getRealms() {
        return realms;
    }

    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    @Override
    public String getPath() {
        return GAMESERVER_PATH;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }
}
