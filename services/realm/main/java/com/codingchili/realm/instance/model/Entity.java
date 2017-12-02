package com.codingchili.realm.instance.model;

import com.codingchili.realm.instance.model.events.Event;
import com.codingchili.realm.instance.model.events.EventType;

import java.util.Collections;
import java.util.Set;

import com.codingchili.core.listener.CoreHandler;

/**
 * @author Robin Duda
 */
public interface Entity extends CoreHandler<Event> {

    Integer getId();

    Vector getVector();

    // todo refactor into list available events by proto.
    default Set<EventType> getInteractions() {
        return Collections.emptySet();
    }

    enum Interactions {INSPECT, TRADE, DIALOG, FRIEND}
}
