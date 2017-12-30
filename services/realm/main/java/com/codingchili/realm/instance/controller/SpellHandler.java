package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Grid;
import com.codingchili.realm.instance.context.Ticker;

import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 */
public class SpellHandler implements Receiver<InstanceRequest> {
    private GameContext game;

    public SpellHandler(GameContext game) {
        this.game = game;
        game.ticker(this::tick, 50);
    }

    private void tick(Ticker ticker) {
        Grid grid = game.creatures();
    }

    // todo; a SpellManager class.
    // todo: get attributes of any entity?
    // todo: get inventory of caster and targets.
    // todo: some entities are not living?

    @Override
    public void handle(InstanceRequest request) {
        // shared SpellManager?
        // needs to cancel on movement etc.
    }
}
