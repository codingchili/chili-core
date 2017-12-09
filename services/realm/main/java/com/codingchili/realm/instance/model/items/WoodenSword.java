package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.Equippable;
import com.codingchili.realm.instance.model.Slot;
import com.codingchili.realm.instance.model.Stats;

public class WoodenSword implements Equippable {
    @Override
    public Slot slot() {
        return Slot.WEAPON;
    }

    @Override
    public void apply(Stats stats) {
        // todo apply this items stats
    }

    @Override
    public String getDescription() {
        return "A wooden sword, quite impressive.";
    }

    @Override
    public String getName() {
        return "Wooden sword";
    }

    // todo: get components, for crafting
    // todo: on hit listener, on damage deal, on damage taken, on target die
}
