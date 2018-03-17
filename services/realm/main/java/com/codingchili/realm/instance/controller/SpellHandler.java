package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.spells.SpellEngine;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 */
public class SpellHandler implements Receiver<Request> {
    private GameContext game;
    private SpellEngine spells;

    public SpellHandler(GameContext game) {
        this.game = game;
        this.spells = game.spells();
        // can retrieve player from the game by id?
    }

   /* public void cast(SpellRequest cast) {
        Creature caster = new Creature();

        // return true = casting begins.
        // this publishes an event.
        spells.cast(caster, request.target(), request.spellName());
    }*/

   // cast, list spells, get spell info.

    public void getSpellInfo(String request) {
        spells.getSpellByName(request);
    }

    @Override
    public void handle(Request request) {
        // shared SpellManager?
        // needs to cancel on movement etc.
    }
}
