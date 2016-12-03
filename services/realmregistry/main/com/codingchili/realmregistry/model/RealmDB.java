package com.codingchili.realmregistry.model;

import com.codingchili.realmregistry.configuration.RealmSettings;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.AsyncStorage;


/**
 * @author Robin Duda
 *         <p>
 *         Shares realmName data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class RealmDB implements AsyncRealmStore {
    private final AsyncStorage<String, RealmSettings> realms;

    public RealmDB(AsyncStorage<String, RealmSettings> map) {
        this.realms = map;
    }

    @Override
    public void getMetadataList(Future<List<RealmMetaData>> future) {

        // FIXME: 2016-12-03 awaiting support for complex queries
        realms.querySimilar("name", "*", map -> {
            if (map.succeeded()) {

                List<RealmMetaData> list = map.result().stream()
                        .map(RealmMetaData::new)
                        .collect(Collectors.toList());

                future.complete(list);
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void signToken(Future<Token> future, String realm, String domain) {
        realms.get(realm, map -> {
            if (map.succeeded()) {
                RealmSettings settings = map.result();
                future.complete(new Token(new TokenFactory(getSecretBytes(settings)), domain));
            } else {
                future.fail(map.cause());
            }
        });
    }

    private byte[] getSecretBytes(RealmSettings settings) {
        return settings.getAuthentication().getKey().getBytes();
    }

    @Override
    public void get(Future<RealmSettings> future, String realmName) {
        realms.get(realmName, map -> {
            if (map.succeeded()) {
                future.complete(map.result());
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void put(Future<Void> future, RealmSettings realm) {
        realms.put(realm.getName(), realm, map -> {
            if (map.succeeded()) {
                future.complete();
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void remove(Future<Void> future, String realmName) {
        realms.remove(realmName, remove -> {
            if (remove.succeeded()) {
                future.complete();
            } else {
                future.fail(remove.cause());
            }
        });
    }
}

