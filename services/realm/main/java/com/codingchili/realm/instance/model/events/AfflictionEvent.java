package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 */
public class AfflictionEvent implements Event {

    @Override
    public EventType getType() {
        return EventType.AFFLICTION;
    }
}
