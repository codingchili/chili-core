package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.model.items.Inventory;
import com.codingchili.realm.instance.model.events.Event;
import com.codingchili.realm.instance.model.spells.AfflictionState;
import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

import com.codingchili.core.listener.Receiver;

/**
 * @author Robin Duda
 */
public interface Entity extends Receiver<Event> {

    String getName();

    Integer getId();

    Vector getVector();

    // the player stats - todo: include inventory stats? or add a Stats.apply?

    // todo: the spell manager can keep track of afflictions and reduce stats when calcing.
    Stats getStats();

    @JsonIgnore
    Stats getBaseStats();

    AfflictionState getAfflictions();

    // todo: can be used by the spell manager to apply on hit effects
    // todo: can be used to calculate the stats
    Inventory getInventory();

    Set<String> getInteractions();
}
