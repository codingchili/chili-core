package com.codingchili.realm.instance.model;

import java.util.Map;

/**
 * @author Robin Duda
 */
public interface Equippable extends Item {

    Slot slot();

    void apply(Stats stats);
}
