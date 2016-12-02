package com.codingchili.services.authentication.model;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.AsyncStorage;

import com.codingchili.services.realm.configuration.RealmSettings;

import static com.codingchili.services.Shared.Strings.COLLECTION_REALMS;

/**
 * @author Robin Duda
 *         <p>
 *         Shares realmName data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class RealmDB implements AsyncRealmStore {
    private final AsyncStorage<String, HashMap<String, RealmSettings>> realms;

    public RealmDB(AsyncStorage<String, HashMap<String, RealmSettings>> map) {
        this.realms = map;

        realms.putIfAbsent(COLLECTION_REALMS, new HashMap<>(), put -> {
            if (put.failed()) {
                throw new RuntimeException(put.cause());
            }
        });
    }

    private void save(Future<Void> future, HashMap<String, RealmSettings> map) {
        realms.put(COLLECTION_REALMS, map, put -> {
            if (put.succeeded()) {
                future.complete();
            } else {
                future.fail(put.cause());
            }
        });
    }

    @Override
    public void getMetadataList(Future<ArrayList<RealmMetaData>> future) {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        realms.get(COLLECTION_REALMS, map -> {

            if (map.succeeded()) {

                list.addAll(map.result().keySet()
                        .stream()
                        .map(key -> new RealmMetaData(map.result().get(key)))
                        .collect(Collectors.toList()));

                future.complete(list);
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void signToken(Future<Token> future, String realm, String domain) {
        realms.get(COLLECTION_REALMS, map -> {
            if (map.succeeded()) {
                RealmSettings settings = map.result().get(realm);

                if (settings == null) {
                    future.fail(new RealmMissingException());
                } else {
                    future.complete(new Token(new TokenFactory(getSecretBytes(settings)), domain));
                }
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
        realms.get(COLLECTION_REALMS, map -> {
            if (map.succeeded()) {
                future.complete(map.result().get(realmName));
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void put(Future<Void> future, RealmSettings realm) {
        realms.get(COLLECTION_REALMS, map -> {
            if (map.succeeded()) {
                HashMap<String, RealmSettings> list = map.result();
                list.put(realm.getName(), realm);
                save(future, list);
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void remove(Future<RealmSettings> future, String realmName) {
        realms.get(COLLECTION_REALMS, get -> {

            if (get.succeeded()) {
                HashMap<String, RealmSettings> map = get.result();
                RealmSettings realm = map.remove(realmName);
                future.complete(realm);
                save(Future.future(), map);
            } else {
                future.fail(get.cause());
            }
        });
    }
}

