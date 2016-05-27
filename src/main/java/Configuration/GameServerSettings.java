package Configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains settings for the game server container.
 */
@JsonIgnoreProperties({"realms", "path", "name"})
public class GameServerSettings implements Configurable {
    static final String GAMESERVER_PATH = "conf/system/gameserver.json";
    static final String REALM_PATH = "conf/realm/";
    private RemoteAuthentication logserver;
    private ArrayList<RealmSettings> realms = new ArrayList<>();

    void setRealms(ArrayList<RealmSettings> realms) {
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
