package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.model.items.Inventory;
import com.codingchili.realm.instance.model.events.Event;
import com.codingchili.realm.instance.model.afflictions.AfflictionState;
import com.codingchili.realm.instance.model.spells.SpellState;
import com.codingchili.realm.instance.model.stats.Stats;

import java.util.Set;

import com.codingchili.core.listener.Receiver;

/**
 * @author Robin Duda
 * <p>
 * An entity models a living creature that may be player controlled or not.
 * Entities can message other entities through Events that are routed
 * through the game context.
 * <p>
 * Entities can travel between worlds/instances, therefore their state must be
 * attached to the entity and not the instances context.
 */
public interface Entity extends Receiver<Event> {

    /**
     * @return the display name of the entity.
     */
    String getName();

    /**
     * @return the unique identifier of the entity.
     */
    Integer getId();

    /**
     * @return the position of the entity in the game world.
     */
    Vector getVector();

    /**
     * @return a set of spells that is available to the entity
     * and their current state, including cooldowns.
     */
    SpellState getSpells();

    /**
     * @return the effective stats that is calculated from the base stats,
     * the equipped items and any afflictions currently active on the entity.
     */
    Stats getStats();

    /**
     * @return the base stats of the entity.
     */
    Stats getBaseStats();

    /**
     * @return contains afflictions currently active on the entity.
     */
    AfflictionState getAfflictions();

    /**
     * @return contains all items in posession by the entity and all
     * items that is equipped.
     */
    Inventory getInventory();

    /**
     * @return a set of names of the events that this entity is a subscriber of.
     */
    Set<String> getInteractions();
}
