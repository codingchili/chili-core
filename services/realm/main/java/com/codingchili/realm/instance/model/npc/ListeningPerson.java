package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.SimpleEntity;
import com.codingchili.realm.instance.model.events.Event;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 */
public class ListeningPerson extends SimpleEntity {
    public static int called = 0;

    public ListeningPerson(GameContext context) {
        super(context);
    }

    @Api(route = "CHAT")
    public void chatevent(Event event) {
     //   ChatEvent chat = ChatEvent.class.cast(event);

   //     System.out.println(chat.getText());
        called += 1;
    }
}
