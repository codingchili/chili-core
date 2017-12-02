package com.codingchili.realm.configuration;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.storage.IndexedMapPersisted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 * Contains settings for the game server container.
 */
@JsonIgnoreProperties({"realms"})
public class RealmServerSettings extends ServiceConfigurable {
    public static final String PATH_REALMSERVER = "conf/service/realmserver.json";
    private List<EnabledRealm> enabled = new ArrayList<>();
    private int realmUpdates = 3500;
    private String storage = IndexedMapPersisted.class.getName();

    /**
     * @return get a list of enabled realms.
     */
    public List<EnabledRealm> getEnabled() {
        return enabled;
    }

    /**
     * @param enabled sets the list of enabled realms.
     */
    public void setEnabled(List<EnabledRealm> enabled) {
        this.enabled = enabled;
    }

    /**
     * @param realm adds a new realm.
     */
    @JsonIgnore
    public void addRealm(EnabledRealm realm) {
        this.enabled.add(realm);
    }

    /**
     * @return get the time in MS which the realm must report to the registry.
     */
    public int getRealmUpdates() {
        return realmUpdates;
    }

    /**
     * @param updateRate set the update rate in MS which realms report to the registry.
     */
    public void setRealmUpdates(int updateRate) {
        this.realmUpdates = updateRate;
    }

    /**
     * @return get the configured storage for the realm.
     */
    public String getStorage() {
        return storage;
    }

    /**
     * @param storage set the configured storage as a classname-string.
     */
    public void setStorage(String storage) {
        this.storage = storage;
    }
}
