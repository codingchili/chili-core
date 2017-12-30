package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;

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

    default Optional<Creature> getSource() {
        return Optional.empty();
    }

    EventType getType();
}
