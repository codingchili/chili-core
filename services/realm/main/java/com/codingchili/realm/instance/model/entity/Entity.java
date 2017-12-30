package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;

import java.util.Optional;
import java.util.Set;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * An entity that exists in the game, can be a player, a house or anything else.
 */
public interface Entity extends Storable, Receiver<Event> {

    /**
     * @return the display name of the entity.
     */
    String getName();

    /**
     * @return the unique identifier of the entity.
     */
    String getId();

    /**
     * @return the position of the entity in the game world.
     */
    Vector getVector();

    /**
     * @return a set of names of the events that this entity is a subscriber of.
     */
    Set<String> getInteractions();

    /**
     * Called after loading the creature to set the context.
     *
     * @param context the game context.
     */
    void setContext(GameContext context);
}
