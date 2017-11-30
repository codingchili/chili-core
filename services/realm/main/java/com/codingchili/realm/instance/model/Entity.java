package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public interface Entity {

    void onEffect(Entity source, Affliction event);

    void onDeath(Entity source);

    void equip(Item item);

    void notify(Object object);

    Position getPosition();
}
