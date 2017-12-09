package com.codingchili.realm.instance.model;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.realm.instance.model.events.Event;

import java.util.Set;

/**
 * @author Robin Duda
 */
public interface Entity extends CoreHandler<Event> {

    Integer getId();

    Vector getVector();

    // the player stats - todo: include inventory stats? or add a Stats.apply?
    // todo: the spell manager can keep track of afflictions and reduce stats when calcing.
    Stats getStats();

    // todo: can be used by the spell manager to apply on hit effects
    // todo: can be used to calculate the stats
    Inventory getInventory();

    Set<String> getInteractions();
}
