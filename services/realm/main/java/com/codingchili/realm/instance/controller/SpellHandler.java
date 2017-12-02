package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.Grid;
import com.codingchili.realm.instance.model.Ticker;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 */
public class SpellHandler implements CoreHandler {
    private GameContext game;

    public SpellHandler(GameContext game) {
        this.game = game;
        game.ticker(this::tick, 1);
    }

    private void tick(Ticker ticker) {
        Grid grid = game.getGrid();
    }

    @Override
    public void handle(Request request) {

    }
}
