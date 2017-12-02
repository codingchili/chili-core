package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.model.Entity;
import com.codingchili.realm.instance.model.Vector;
import com.codingchili.realm.instance.model.events.ChatEvent;
import com.codingchili.realm.instance.model.events.Event;

import java.util.UUID;

/**
 * @author Robin Duda
 */
public class ListeningPerson implements Entity {
    private Integer id = UUID.randomUUID().hashCode();

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void notify(Event event) {
        if (event.getType() == Event.Type.CHAT) {
            System.out.println(id + " received event " + ((ChatEvent) event).getText());
        }
    }

    @Override
    public Vector getVector() {
        return new Vector().setX(100).setY(100);
    }
}
