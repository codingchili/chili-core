package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;

import com.codingchili.core.listener.*;


/**
 * @author Robin Duda
 */
public class DialogHandler implements Receiver<Request> {
    private GameContext game;

    public DialogHandler(GameContext game) {
        this.game = game;
    }

    @Override
    public void handle(Request request) {

    }
}
