package com.codingchili.realm;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.model.entity.PlayerCharacter;
import com.codingchili.realm.instance.model.entity.PlayerClass;
import com.codingchili.realm.model.AsyncCharacterStore;
import com.codingchili.realm.model.CharacterDB;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 * <p>
 * Context mock for realms.
 */
public class ContextMock extends RealmContext {
    private RealmSettings realm = new RealmSettings();

    public ContextMock() {
        this(new SystemContext());
    }

    public ContextMock(CoreContext context) {
        super(context);

        realm = new RealmSettings()
                .setName("realmName")
                .setAuthentication(new Token(new TokenFactory("s".getBytes()), "realmName"));
        realm.getClasses().add(new PlayerClass().setName("class.name"));
    }

    @Override
    public Future<AsyncCharacterStore> getCharacterStore(RealmSettings settings) {
        Future<AsyncCharacterStore> future = Future.future();

        new StorageLoader<PlayerCharacter>().sharedmap(new StorageContext<PlayerCharacter>(this))
                .withDB("", "")
                .withValue(PlayerCharacter.class)
                .build(storage -> {
                    future.complete(new CharacterDB(storage.result()));
                });
        return future;
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(realm.getTokenBytes());
    }

    @Override
    public RealmSettings realm() {
        return realm;
    }
}
