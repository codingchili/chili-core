package com.codingchili.services.Authentication.Model;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.codingchili.core.Security.Token;
import com.codingchili.core.Security.TokenFactory;

import com.codingchili.services.Realm.Configuration.RealmSettings;

import static com.codingchili.services.Shared.Strings.MAP_REALMS;

/**
 * @author Robin Duda
 *         <p>
 *         Shares realmName data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class HazelRealmDB implements AsyncRealmStore {
    private final AsyncMap<String, HashMap<String, RealmSettings>> realms;

    public static void create(Future<AsyncRealmStore> future, Vertx vertx) {
        vertx.sharedData().<String, HashMap<String, RealmSettings>>getClusterWideMap(MAP_REALMS, cluster -> {
            if (cluster.succeeded()) {
                AsyncMap<String, HashMap<String, RealmSettings>> realms = cluster.result();
                future.complete(new HazelRealmDB(realms));
            } else {
                future.fail(cluster.cause());
            }
        });
    }

    HazelRealmDB(AsyncMap<String, HashMap<String, RealmSettings>> map) {
        this.realms = map;

        realms.putIfAbsent(MAP_REALMS, new HashMap<>(), put -> {
            if (put.failed()) {
                throw new RuntimeException(put.cause());
            }
        });
    }

    private void save(HashMap<String, RealmSettings> map) {
        realms.put(MAP_REALMS, map, put -> {
            if (put.failed()) {
                throw new RuntimeException(put.cause());
            }
        });
    }

    @Override
    public void getMetadataList(Future<ArrayList<RealmMetaData>> future) {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        realms.get(MAP_REALMS, map -> {

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
        realms.get(MAP_REALMS, map -> {
            RealmSettings settings = map.result().get(realm);

            if (settings != null) {
                if (map.result() == null) {
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
        realms.get(MAP_REALMS, map -> {
            if (map.succeeded()) {

                if (map.result() == null) {
                    future.fail(new RealmMissingException());
                } else {
                    future.complete(map.result().get(realmName));
                }
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void put(Future<Void> future, RealmSettings realm) {
        realms.get(MAP_REALMS, map -> {
            if (map.succeeded()) {
                HashMap<String, RealmSettings> list = map.result();
                list.put(realm.getName(), realm);
                future.complete();
                save(list);
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void remove(Future<RealmSettings> future, String realmName) {
        realms.get(MAP_REALMS, get -> {

            if (get.succeeded()) {
                HashMap<String, RealmSettings> map = get.result();
                RealmSettings realm = map.remove(realmName);
                future.complete(realm);
                save(map);
            } else {
                future.fail(get.cause());
            }
        });
    }
}

