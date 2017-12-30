package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.entity.Creature;

import java.util.*;

/**
 * @author Robin Duda
 */
public class Recipe extends ArrayList<RecipeEntry> {

    public Recipe add(String item, Integer quantity) {
        add(new RecipeEntry().setName(item).setQuantity(quantity));
        return this;
    }

    public Recipe tool(String item) {
        add(new RecipeEntry().setName(item).setQuantity(1).setConsumed(false));
        return this;
    }

    public Optional<Item> craft(Creature creature) {

        // todo check requisites.
        // todo: deduct requisites.
        // todo: craft item - randomize stats/afflictions/rarity scoring.
        // todo: give item.

        return Optional.empty();
    }
}
