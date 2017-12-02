package com.codingchili.realm.instance.model.events;

/**
 * @author Robin Duda
 * <p>
 * Events are sent to clients as notifications.
 */
public interface Event {

    Type getType();

    enum Type {SPELL, MOVEMENT, DEATH, AFFLICTION, SPAWN, CHAT}
}
