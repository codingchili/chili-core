package com.codingchili.core.Realm.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 */
public class EnabledRealm {
    private String realm;
    private List<String> instances = new ArrayList<>();

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public List<String> getInstances() {
        return instances;
    }

    public void setInstances(List<String> instances) {
        this.instances = instances;
    }
}
