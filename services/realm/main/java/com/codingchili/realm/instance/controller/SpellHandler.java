package com.codingchili.realm.instance.controller;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.Grid;
import com.codingchili.realm.instance.model.Ticker;

/**
 * @author Robin Duda
 */
public class SpellHandler implements CoreHandler<Request> {
    private GameContext game;

    public SpellHandler(GameContext game) {
        this.game = game;
        game.ticker(this::tick, 50);
    }

    private void tick(Ticker ticker) {
        Grid grid = game.getGrid();
    }

    // todo; a SpellManager class.
    // todo: get attributes of any entity?
    // todo: get inventory of caster and targets.
    // todo: some entities are not living?

    @Override
    public void handle(Request request) {
        // shared SpellManager?
        // needs to cancel on movement etc.
    }
}
