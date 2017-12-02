package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.Ticker;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 */
public class MovementHandler implements CoreHandler<Request> {
    private GameContext game;

    public MovementHandler(GameContext game) {
        this.game = game;

        game.ticker(this::update, 1);
    }

    private void update(Ticker ticker) {
        // todo: for all entities update position based on
        // movement direction and speed. possible to split moving entities and non moving entities?
        // no collision from player-player is required - that is on the client side for now.
    }

    @Override
    public void handle(Request request) {
        // todo handle player input
    }

    // todo write to client: easy just near the game context.
}
