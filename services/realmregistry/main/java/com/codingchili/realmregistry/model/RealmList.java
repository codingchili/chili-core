package com.codingchili.realmregistry.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 */
public class RealmList {
    private List<RealmMetaData> realms = new ArrayList<>();

    public RealmList() {
    }

    public RealmList(List<RealmMetaData> realms) {
        this.realms = realms;
    }

    public List<RealmMetaData> getRealms() {
        return realms;
    }

    public void setRealms(List<RealmMetaData> realms) {
        this.realms = realms;
    }
}
