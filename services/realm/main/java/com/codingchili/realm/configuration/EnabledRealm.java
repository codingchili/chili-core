package com.codingchili.realm.configuration;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static com.codingchili.common.Strings.*;

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

    @JsonIgnore
    public String getPath() {
        return PATH_REALM + realm + EXT_JSON;
    }
}
