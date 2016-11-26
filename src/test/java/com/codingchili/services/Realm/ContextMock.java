package com.codingchili.services.realm;

import io.vertx.core.Vertx;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.security.*;
import com.codingchili.core.storage.PrivateMap;

import com.codingchili.services.realm.configuration.RealmContext;
import com.codingchili.services.realm.configuration.RealmSettings;
import com.codingchili.services.realm.instance.model.PlayerClass;
import com.codingchili.services.realm.model.AsyncCharacterStore;
import com.codingchili.services.realm.model.CharacterDB;

/**
 * @author Robin Duda
 */
public class ContextMock extends RealmContext {
    private RealmSettings realm = new RealmSettings();
    private AsyncCharacterStore characters;

    public ContextMock(Vertx vertx) {
        super(vertx);

        realm = new RealmSettings()
                .setName("realmName")
                .setAuthentication(new Token(new TokenFactory("s".getBytes()), "realmName"));

        realm.getClasses().add(new PlayerClass().setName("class.name"));

        characters = new CharacterDB(new PrivateMap<>(new StorageContext(this)));
    }

    @Override
    public AsyncCharacterStore getCharacterStore() {
        return characters;
    }

    TokenFactory getClientFactory() {
        return new TokenFactory(realm.getTokenBytes());
    }

    @Override
    public RealmSettings realm() {
        return realm;
    }
}
