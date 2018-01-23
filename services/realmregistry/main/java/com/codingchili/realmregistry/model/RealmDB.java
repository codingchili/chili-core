package com.codingchili.realmregistry.model;

import com.codingchili.realmregistry.configuration.RegisteredRealm;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.*;

import static com.codingchili.core.configuration.CoreStrings.*;
import static io.vertx.core.Future.*;


/**
 * @author Robin Duda
 * <p>
 * Shares realm data between the clienthandler and the realmhandler.
 * Allows the deployment of multiple handlers.
 */
public class RealmDB implements AsyncRealmStore {
    private static final String STALE_REALM_WATCHER = "stale realm watcher";
    private final AsyncStorage<RegisteredRealm> realms;
    private EntryWatcher<RegisteredRealm> watcher;
    private int timeout = 15000;

    public RealmDB(AsyncStorage<RegisteredRealm> map) {
        this.realms = map;

        this.watcher = new EntryWatcher<>(realms, this::getStaleQuery, this::getTimeout)
                .start(items -> items.forEach(item ->
                        realms.remove(item.getId(), (removed) -> {
                            if (removed.failed()) {
                                map.context().logger(getClass()).onError(removed.cause());
                            }
                        })));
    }

    private QueryBuilder<RegisteredRealm> getStaleQuery() {
        return realms.query(ID_UPDATED).between(0L, getLastValidTime()).setName(STALE_REALM_WATCHER);
    }

    private long getLastValidTime() {
        return Instant.now().toEpochMilli() - timeout;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public RealmDB setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public void getMetadataList(Handler<AsyncResult<List<RealmMetaData>>> future) {
        realms.values(map -> {
            if (map.succeeded()) {
                List<RealmMetaData> list = map.result().stream()
                        .map(RealmMetaData::new)
                        .collect(Collectors.toList());

                future.handle(succeededFuture((list)));
            } else {
                future.handle(failedFuture(map.cause()));
            }
        });
    }

    @Override
    public void signToken(Handler<AsyncResult<Token>> future, String realm, String domain) {
        realms.query(ID_NAME).equalTo(realm).execute(map -> {
            Collection<RegisteredRealm> realms = map.result();
            if (map.succeeded() && realms.size() > 0) {
                RegisteredRealm settings = realms.iterator().next();
                future.handle(succeededFuture(new Token(new TokenFactory(getSecretBytes(settings)), domain)));
            } else {
                future.handle(failedFuture(map.cause()));
            }
        });
    }

    private byte[] getSecretBytes(RegisteredRealm settings) {
        return settings.getAuthentication().getKey().getBytes();
    }

    @Override
    public void get(Handler<AsyncResult<RegisteredRealm>> future, String realmName) {
        realms.get(realmName, map -> {
            if (map.succeeded()) {
                future.handle(succeededFuture(map.result()));
            } else {
                future.handle(failedFuture(map.cause()));
            }
        });
    }

    @Override
    public void put(Handler<AsyncResult<Void>> future, RegisteredRealm realm) {
        realms.put(realm, future);
    }

    @Override
    public void remove(Handler<AsyncResult<Void>> future, String realmName) {
        realms.remove(realmName, future);
    }
}

