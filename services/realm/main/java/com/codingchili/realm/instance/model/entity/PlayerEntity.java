package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.context.*;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * model for player characters.
 */
public class PlayerEntity extends SimpleEntity {

    public PlayerEntity(GameContext context) {
        super(context);


        // todo: set name.
        protocol.annotated(this);
    }

    public static void main(String[] args) {
        GameContext game = new GameContext(new InstanceContext(
                new RealmContext(
                        new SystemContext(),
                        new RealmSettings()),
                new InstanceSettings()));


        System.out.println(Serializer.pack(new PlayerEntity(game)));
    }
}
