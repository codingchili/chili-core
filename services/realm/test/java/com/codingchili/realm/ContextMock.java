package com.codingchili.realm;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.model.PlayerCharacter;
import com.codingchili.realm.instance.model.PlayerClass;
import com.codingchili.realm.model.AsyncCharacterStore;
import com.codingchili.realm.model.CharacterDB;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 * <p>
 * Context mock for realms.
 */
public class ContextMock extends RealmContext {
    private RealmSettings realm = new RealmSettings();
    private AsyncCharacterStore characters;

    public ContextMock(Vertx vertx) {
        super(new SystemContext(vertx));

        realm = new RealmSettings()
                .setName("realmName")
                .setAuthentication(new Token(new TokenFactory("s".getBytes()), "realmName"));

        realm.getClasses().add(new PlayerClass().setName("class.name"));

        new StorageLoader<PlayerCharacter>().privatemap(new StorageContext<PlayerCharacter>(this))
                .withDB("", "")
                .withClass(PlayerCharacter.class)
                .build(storage -> {
                    characters = new CharacterDB(storage.result());
                });
    }

    @Override
    public AsyncCharacterStore getCharacterStore() {
        return characters;
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(realm.getTokenBytes());
    }

    @Override
    public RealmSettings realm() {
        return realm;
    }
}
