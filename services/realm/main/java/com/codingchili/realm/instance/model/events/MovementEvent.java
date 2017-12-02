package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 */
public class MovementEvent implements Event {
    @Override
    public EventType getType() {
        return EventType.MOVEMENT;
    }
}
