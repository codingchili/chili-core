package com.codingchili.realm.instance.model;

import com.codingchili.realm.instance.model.events.Event;

import java.io.Serializable;

/**
 * @author Robin Duda
 * model for portals used to travel between maps.
 */
public class Portal implements Entity {

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
}
