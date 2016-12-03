package com.codingchili.realmregistry.model;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
public class RealmList {
    private ArrayList<RealmMetaData> realms = new ArrayList<>();

    public RealmList(ArrayList<RealmMetaData> realms) {
        this.realms = realms;
    }

    public ArrayList<RealmMetaData> getRealms() {
        return realms;
    }

    public void setRealms(ArrayList<RealmMetaData> realms) {
        this.realms = realms;
    }
}
