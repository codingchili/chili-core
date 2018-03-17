package com.codingchili.realm.instance.model.events;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 * <p>
 * Events are sent to clients as notifications.
 * Unicast updates should be sent directly to the entity instead.
 */
public interface Event {

    /**
     * the target group as defined by the server.
     *
     * @return the scope to use for the event when broadcasting, for example
     * network partitioning etc.
     */
    @JsonIgnore // the clients does not need to know this.
    default Broadcast getBroadcast() {
        if (getSource() != null) {
            return Broadcast.PARTITION;
        } else {
            return Broadcast.GLOBAL;
        }
    }

    /**
     * @return the source of the event.
     */
    default String getSource() {
        return null;
    }

    /**
     * @return the type of event.
     */
    EventType getType();
}
