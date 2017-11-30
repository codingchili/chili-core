package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public interface Player {

    void notify(String message);

    void give(Item item);

    Inventory inventory();
}
