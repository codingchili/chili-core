package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.model.entity.PlayerCreature;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;

/**
 * @author Robin Duda
 *
 * A message to an instance that a player is joining.
 */
public class InstanceRequest extends RequestWrapper {
    private PlayerCreature player;
    private String realmName;

    /**
     * Constructor for outbound messages.
     */
    public InstanceRequest() {
        super(null);
    }

    /**
     * Constructor for inbound messages.
     * @param request the request to map as an instance request.
     */
    public InstanceRequest(Request request) {
        super(request);
    }


    public PlayerCreature getPlayer() {
        return player;
    }

    public InstanceRequest setPlayer(PlayerCreature player) {
        this.player = player;
        return this;
    }

    public String getRealmName() {
        return realmName;
    }

    public InstanceRequest setRealmName(String realmName) {
        this.realmName = realmName;
        return this;
    }
}
