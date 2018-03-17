package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.items.Item;

import java.util.Collection;

import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 */
public class TradeHandler implements Receiver<Request> {
    private Creature initiator;
    private Creature other;
    private Collection<Item> initiatorItems;
    private Collection<Item> otherItems;
    private GameContext game;

    public TradeHandler(GameContext game) {
        this.game = game;
    }

    public void offer(Creature creature, Item item) {
        // notify other entity of added item
    }

    public void remove(Creature creature, Item item) {
        // notify other entity of removed item.
    }

    public void accept(Creature creature) {
        // both entities accept the trade.
        // transfer items here, lock entities inventories.
    }

    public void complete(Creature creature) {
        // both entities completes the trade.
    }

    @Override
    public void handle(Request request) {
        // todo check event type.
    }
}
