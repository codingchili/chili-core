package com.codingchili.realm.instance.model.afflictions;

import java.util.ArrayList;
import java.util.Collection;

/**
 * List holder for serialization.
 */
public class AfflictionList {
    private Collection<Affliction> afflictions = new ArrayList<>();

    public Collection<Affliction> getAfflictions() {
        return afflictions;
    }

    public void setAfflictions(Collection<Affliction> afflictions) {
        this.afflictions = afflictions;
    }
}
