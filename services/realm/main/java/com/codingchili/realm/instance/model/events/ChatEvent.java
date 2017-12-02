package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.Entity;

/**
 * @author Robin Duda
 */
public class ChatEvent implements Event {
    private Entity entity;
    private String text;

    public ChatEvent(Entity source, String text) {
        this.entity = source;
        this.text = text;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getText() {
        return text;
    }

    @Override
    public Type getType() {
        return Type.CHAT;
    }
}
