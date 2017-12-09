package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.*;

public class WoodenSword extends Item {
    {
        slot = Slot.weapon;
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
