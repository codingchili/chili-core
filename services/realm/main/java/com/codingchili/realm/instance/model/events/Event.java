package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.Broadcast;
import com.codingchili.realm.instance.model.Entity;

import java.util.Optional;

/**
 * @author Robin Duda
 * <p>
 * Events are sent to clients as notifications.
 */
public interface Event {

    default Broadcast getBroadcast() {
        if (getSource().isPresent()) {
            return Broadcast.PARTITION;
        } else {
            return Broadcast.GLOBAL;
        }
    }

    default Optional<Entity> getSource() {
        return Optional.empty();
    }

    EventType getType();
}
