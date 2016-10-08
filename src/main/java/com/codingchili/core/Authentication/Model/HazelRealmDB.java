package com.codingchili.core.Authentication.Model;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Authentication.RealmMetaData;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 *         <p>
 *         Shares realmName data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class HazelRealmDB implements AsyncRealmStore {
    // A standard map is stored within the map so that it is possible to enumerate available realms.
    private AsyncMap<String, HashMap<String, RealmSettings>> realms;

    public static void create(Future<AsyncRealmStore> future, Vertx vertx) {
        vertx.sharedData().<String, HashMap<String, RealmSettings>>getClusterWideMap(Strings.MAP_REALMS, cluster -> {
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

        realms.putIfAbsent(Strings.MAP_REALMS, new HashMap<>(), put -> {
            if (put.failed()) {
                throw new RuntimeException(put.cause());
            }
        });
    }

    @Override
    public void getMetadataList(Future<ArrayList<RealmMetaData>> future) {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        realms.get(Strings.MAP_REALMS, map -> {

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
        realms.get(Strings.MAP_REALMS, map -> {
            RealmSettings settings = map.result().get(realm);

            if (map.succeeded()) {
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
        return settings.getAuthentication().getToken().getKey().getBytes();
    }

    @Override
    public void get(Future<RealmSettings> future, String realmName) {
        realms.get(Strings.MAP_REALMS, map -> {
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
        realms.get(Strings.MAP_REALMS, map -> {
            if (map.succeeded()) {
                HashMap<String, RealmSettings> list = map.result();

                list.put(realm.getName(), realm);

                realms.put(Strings.MAP_REALMS, list, put -> {
                    if (put.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(put.cause());
                    }
                });
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void remove(Future<Void> future, String realmName) {
        realms.get(Strings.MAP_REALMS, get -> {

            if (get.succeeded()) {
                HashMap<String, RealmSettings> map = get.result();

                map.remove(realmName);

                realms.replace(Strings.MAP_REALMS, map, replace -> {
                    if (replace.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(replace.cause());
                    }
                });
            } else {
                future.fail(get.cause());
            }
        });
    }

    @Override
    public void update(Future<Void> future, String realmName, int players) {
        realms.get(Strings.MAP_REALMS, map -> {

            if (map.succeeded()) {
                RealmSettings realm = map.result().get(realmName);

                if (realm != null) {
                    realm.setPlayers(players);

                    realms.replace(Strings.MAP_REALMS, map.result(), put -> {
                        if (put.succeeded()) {
                            future.complete();
                        } else {
                            future.fail(put.cause());
                        }
                    });
                } else {
                    future.fail(new RealmMissingException());
                }
            } else {
                future.fail(map.cause());
            }
        });
    }
}

