package com.codingchili.realmregistry;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.realmregistry.configuration.RealmRegistrySettings;
import com.codingchili.realmregistry.configuration.RegisteredRealm;
import com.codingchili.realmregistry.configuration.RegistryContext;
import com.codingchili.realmregistry.model.AsyncRealmStore;
import com.codingchili.realmregistry.model.RealmDB;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 */
public class ContextMock extends RegistryContext {

    public ContextMock() {
        this(new SystemContext());
    }

    public ContextMock(CoreContext context) {
        super(context);
        this.realmFactory = new TokenFactory(new RealmRegistrySettings().getRealmSecret());
    }

    @Override
    public void getRealmStore(Handler<AsyncResult<AsyncRealmStore>> handler) {
        new StorageLoader<RegisteredRealm>().privatemap(new StorageContext<>(this))
                .withValue(RegisteredRealm.class)
                .withDB("", "")
                .build(result -> {
                    this.realms = new RealmDB(result.result());

                    RegisteredRealm realm = new RegisteredRealm()
                            .setName("realmName")
                            .setAuthentication(new Token(new TokenFactory("s".getBytes()), "realmName"));

                    realms.put(Future.future(), realm);
                    handler.handle(Future.succeededFuture(realms));
                });
    }

    public TokenFactory getClientFactory() {
        return new TokenFactory(new RealmRegistrySettings().getClientSecret());
    }
}
