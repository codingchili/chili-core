package com.codingchili.realm.instance.model;

import com.codingchili.realm.instance.model.events.Event;

/**
 * @author Robin Duda
 */
public interface Entity extends Interactable {

    Integer getId();

    void notify(Event event);

    Vector getVector();
}
