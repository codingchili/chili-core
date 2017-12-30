package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.context.GameContext;

import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * model for player characters.
 */
public class PlayerCreature extends SimpleCreature {
    private String account;

    public PlayerCreature() {
    }

    public PlayerCreature(String id) {
        this.id = id;
        this.name = id;
    }

    @Override
    public void setContext(GameContext context) {
        protocol.annotated(this);
        context.subscribe(this);
        super.setContext(context);
    }

    public String getAccount() {
        return account;
    }

    public PlayerCreature setAccount(String account) {
        this.account = account;
        return this;
    }

    public static void main(String[] args) {
        System.out.println(Serializer.pack(new PlayerCreature()));
    }
}
