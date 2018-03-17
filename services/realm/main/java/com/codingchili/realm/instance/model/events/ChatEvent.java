package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;

import java.util.Optional;

/**
 * @author Robin Duda
 */
public class ChatEvent implements Event {
    private String source;
    private String text;

    public ChatEvent(Creature source, String text) {
        this.source = source.getId();
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public EventType getType() {
        return EventType.CHAT;
    }
}
