package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 * <p>
 * Emitted to clients - requires a response.
 * Used to check that clients are still connected.
 */
public class PingEvent implements Event {

    @Override
    public EventType getType() {
        return EventType.ANY;
    }
}
