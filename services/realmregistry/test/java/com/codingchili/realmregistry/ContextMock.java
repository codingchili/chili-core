package com.codingchili.realmregistry;

import com.codingchili.realmregistry.configuration.*;
import com.codingchili.realmregistry.model.AsyncRealmStore;
import com.codingchili.realmregistry.model.RealmDB;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;

/**
 * @author Robin Duda
 */
public class ContextMock extends RegistryContext {

    public ContextMock(Vertx vertx) {
        super(vertx);

        this.realmFactory = new TokenFactory(new RealmRegistrySettings().getRealmSecret());

        new StorageLoader<RealmSettings>().privatemap(new StorageContext<>(vertx))
                .withClass(RealmSettings.class)
                .withDB("", "")
                .build(result -> {
                    this.realms = new RealmDB(result.result());
                });

        RealmSettings realm = new RealmSettings()
                .setName("realmName")
                .setAuthentication(new Token(new TokenFactory("s".getBytes()), "realmName"));

        realm.getClasses().add("class.name");

        realms.put(Future.future(), realm);
    }

    @Override
    public AsyncRealmStore getRealmStore() {
        return realms;
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(new RealmRegistrySettings().getClientSecret());
    }
}
