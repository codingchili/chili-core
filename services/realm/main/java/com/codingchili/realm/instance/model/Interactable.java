package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public interface Interactable {

    boolean isInspectable();

    default void inspect(Entity entity) {}

    boolean isTradeable();

    TradeHandler trade(Entity entity);

    void isDialogable();

    default void dialogue(Entity entity) {}

    void isFriendable();

    default void friend(Entity other) {};
}
