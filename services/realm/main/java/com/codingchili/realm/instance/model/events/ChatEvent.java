package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;

import java.util.Optional;

/**
 * @author Robin Duda
 */
public class ChatEvent implements Event {
    private Creature creature;
    private String text;

    public ChatEvent(Creature source, String text) {
        this.creature = source;
        this.text = text;
    }

    public Creature getCreature() {
        return creature;
    }

    public String getText() {
        return text;
    }

    @Override
    public Optional<Creature> getSource() {
        return Optional.of(creature);
    }

    @Override
    public EventType getType() {
        return EventType.CHAT;
    }
}
