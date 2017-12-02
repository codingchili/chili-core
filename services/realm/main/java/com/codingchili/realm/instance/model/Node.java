package com.codingchili.realm.instance.model;

import com.codingchili.realm.instance.model.events.Event;

import java.util.*;

/**
 * @author Robin Duda
 * Contains a lootable object.
 */
public class Node implements Entity, Interactable {

    @Override
    public Integer getId() {
        return null;
    }

    @Override
    public void notify(Event event) {

    }

    @Override
    public Vector getVector() {
        return null;
    }

    @Override
    public Set<Interactions> getInteractions() {
        return Collections.emptySet();
    }
}
