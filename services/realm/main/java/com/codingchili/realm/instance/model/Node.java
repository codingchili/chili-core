package com.codingchili.realm.instance.model;

import com.codingchili.realm.instance.model.events.Event;

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
    public Vector getVector() {
        return null;
    }

    @Override
    public void handle(Event request) {

    }
}
