package com.codingchili.realm.configuration;

import com.codingchili.realm.instance.model.entity.PlayableClass;
import com.codingchili.realm.model.AsyncCharacterStore;
import com.codingchili.realm.model.CharacterDB;
import io.vertx.core.Future;

import com.codingchili.core.context.*;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.SharedMap;

/**
 * @author Robin Duda
 * <p>
 * Context mock for realms.
 */
public class ContextMock extends RealmContext {

    public ContextMock() {
        this(new SystemContext());
    }

    public ContextMock(CoreContext context) {
        super(context,new RealmSettings()
                .setName("realmName")
                .setAuthentication(new Token(new TokenFactory("s".getBytes()), "realmName")));

        super.getClasses().add(new PlayableClass().setName("class.name"));
    }

    @Override
    public AsyncCharacterStore characters() {
        // the sharedMap is set up synchronously.
        return new CharacterDB(new SharedMap<>(Future.future(), new StorageContext<>(this)));
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(realm().getTokenBytes());
    }

    @Override
    public RealmSettings realm() {
        return super.realm();
    }
}
