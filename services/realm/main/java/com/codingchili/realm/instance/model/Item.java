package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public interface Item {

    String getDescription();

    String getName();

    boolean isEquippable();

    default void equip(Entity entity) {};

    boolean isUsable();

    default void use(Entity entity) {};
}
