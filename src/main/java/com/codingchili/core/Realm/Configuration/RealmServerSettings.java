package com.codingchili.core.Realm.Configuration;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Configuration.Strings;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains settings for the game server container.
 */
@JsonIgnoreProperties({"realms"})
public class RealmServerSettings implements Configurable {
    private RemoteAuthentication logserver;
    private ArrayList<EnabledRealm> enabled = new ArrayList<>();

    public ArrayList<EnabledRealm> getEnabled() {
        return enabled;
    }

    public void setEnabled(ArrayList<EnabledRealm> enabled) {
        this.enabled = enabled;
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
        return Strings.PATH_REALMSERVER;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }
}
