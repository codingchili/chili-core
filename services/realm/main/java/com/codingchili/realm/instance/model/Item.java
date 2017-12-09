package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public interface Item {

    String getDescription();

    String getName();

    default boolean isEquippable() {
        return this instanceof Equippable;
    }

    default boolean isUsable() {
        return this instanceof Usable;
    }
}
