package com.codingchili.realm.instance.model;

import java.util.ArrayList;
import java.util.Collection;

public class AfflictionList {
    private Collection<Affliction> list = new ArrayList<>();

    public Collection<Affliction> getList() {
        return list;
    }

    public void setList(Collection<Affliction> list) {
        this.list = list;
    }
}
