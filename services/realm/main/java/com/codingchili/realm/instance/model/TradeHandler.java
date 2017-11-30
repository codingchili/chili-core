package com.codingchili.realm.instance.model;

import java.util.Collection;

/**
 * @author Robin Duda
 */
public class TradeHandler {
    private Entity initiator;
    private Entity other;
    private Collection<Item> initiatorItems;
    private Collection<Item> otherItems;

    public static TradeHandler request(Entity initiator, Entity other) {
        // todo: make sure only one trade is open per Entity: reject the trade if not.
        // todo: notify the request.
        // todo: make this into a future somehow.
        // todo: notify entities if trades are opened.
        return new TradeHandler();
    }

    public void offer(Entity entity, Item item) {
        // notify other entity of added item
    }

    public void remove(Entity entity, Item item) {
        // notify other entity of removed item.
    }

    public void accept(Entity entity) {
        // both entities accept the trade.
        // transfer items here, lock entities inventories.
    }

    public void complete(Entity entity) {
        // both entities completes the trade.
    }
}
