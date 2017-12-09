package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 */
public class ShutdownEvent implements Event {

    @Override
    public EventType getType() {
        return EventType.SHUTDOWN;
    }
}
