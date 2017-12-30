package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.model.entity.SimpleCreature;
import com.codingchili.realm.instance.model.events.Event;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 */
public class ListeningPerson extends SimpleCreature {
    public static int called = 0;

    @Api(route = "CHAT")
    public void chatevent(Event event) {
        //   ChatEvent chat = ChatEvent.class.cast(event);
        //     System.out.println(chat.getText());
        called += 1;
    }
}
