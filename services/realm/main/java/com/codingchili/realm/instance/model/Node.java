package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 * Contains a lootable object.
 */
public class Node implements Entity, Interactable {

    @Override
    public void interact(Entity entity) {
        // do something.
    }

    @Override
    public void onEffect(Entity source, EffectEvent event) {
        // only affected by physical damage, if player has the right tool equipped etc.
    }

    @Override
    public void onDeath(Entity source) {
        // give player resource
    }
}
