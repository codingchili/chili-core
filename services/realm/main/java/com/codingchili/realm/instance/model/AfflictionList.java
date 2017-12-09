package com.codingchili.realm.instance.model;

import java.util.ArrayList;
import java.util.Collection;

public class AfflictionList {
    private Collection<Affliction> afflictions = new ArrayList<>();

    public Collection<Affliction> getAfflictions() {
        return afflictions;
    }

    public void setAfflictions(Collection<Affliction> afflictions) {
        this.afflictions = afflictions;
    }
}
