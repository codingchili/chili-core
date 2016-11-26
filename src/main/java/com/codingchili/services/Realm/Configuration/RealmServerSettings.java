package com.codingchili.services.realm.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

import com.codingchili.core.configuration.ServiceConfigurable;

/**
 * @author Robin Duda
 *         Contains settings for the game server container.
 */
@JsonIgnoreProperties({"realms"})
public class RealmServerSettings extends ServiceConfigurable {
    public static final String PATH_REALMSERVER = "conf/service/realmserver.json";
    private ArrayList<EnabledRealm> enabled = new ArrayList<>();
    private int realmUpdates = 3500;

    public ArrayList<EnabledRealm> getEnabled() {
        return enabled;
    }

    public void setEnabled(ArrayList<EnabledRealm> enabled) {
        this.enabled = enabled;
    }

    public void setRealmUpdates(int updateRate) {
        this.realmUpdates = updateRate;
    }

    public int getRealmUpdates() {
        return realmUpdates;
    }
}
