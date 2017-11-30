package com.codingchili.realm.instance.model;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 */
public class DialogHandler implements CoreHandler, ConnectionAware {
    private Map<String, Client> clients = new HashMap<>();

    // todo: request: check if target is interactable.

    // todo: must be able to access all players.

    // todo: create a current game context for player/world data

    @Override
    public void handle(Request request) {
        clients.forEach((k, v) -> {
            v.write(new JsonObject().put("ping", request.data()));
        });
    }

    @Override
    public void onConnect(Client client) {
        client.setAuthenticated(true);
        clients.put(client.id(), client);
    }

    @Override
    public void onDisconnect(Client client) {
        clients.remove(client.id(), client);
    }
}
