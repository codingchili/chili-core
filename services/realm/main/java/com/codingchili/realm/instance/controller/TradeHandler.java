package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.*;

import java.util.Collection;

import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 */
public class TradeHandler implements Receiver<InstanceRequest> {
    private Entity initiator;
    private Entity other;
    private Collection<Item> initiatorItems;
    private Collection<Item> otherItems;
    private GameContext game;

    public TradeHandler(GameContext game) {
        this.game = game;
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

    @Override
    public void handle(InstanceRequest request) {
        // todo check event type.
    }
}
