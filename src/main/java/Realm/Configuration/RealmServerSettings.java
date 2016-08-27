package Realm.Configuration;

import Configuration.Configurable;
import Configuration.RemoteAuthentication;
import Configuration.Strings;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains settings for the game server container.
 */
@JsonIgnoreProperties({"realms"})
public class RealmServerSettings implements Configurable {
    private RemoteAuthentication logserver;
    private ArrayList<RealmSettings> realms = new ArrayList<>();

    public void setRealms(ArrayList<RealmSettings> realms) {
        this.realms = realms;
    }

    public ArrayList<RealmSettings> getRealms() {
        return realms;
    }

    @Override
    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    @Override
    public String getPath() {
        return Strings.PATH_GAMESERVER;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }
}
