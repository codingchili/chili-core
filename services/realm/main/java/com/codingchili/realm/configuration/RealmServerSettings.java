package com.codingchili.realm.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.storage.IndexedMap;

/**
 * @author Robin Duda
 *         Contains settings for the game server container.
 */
@JsonIgnoreProperties({"realms"})
public class RealmServerSettings extends ServiceConfigurable {
    public static final String PATH_REALMSERVER = "conf/service/realmserver.json";
    private ArrayList<EnabledRealm> enabled = new ArrayList<>();
    private int realmUpdates = 3500;
    private String storage = IndexedMap.class.getName();

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

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
