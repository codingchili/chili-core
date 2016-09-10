package Authentication.Model;

import Configuration.Strings;
import Protocols.Authentication.RealmMetaData;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Realm.Configuration.RealmSettings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 *         <p>
 *         Shares realmName data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class RealmStore {
    // A standard map is stored within the map so that it is possible to enumerate available realms.
    private AsyncMap<String, HashMap<String, RealmSettings>> realms;

    public RealmStore(Vertx vertx) {
        vertx.sharedData().<String, HashMap<String, RealmSettings>>getClusterWideMap(Strings.MAP_REALMS, cluster -> {
            if (cluster.succeeded()) {
                realms = cluster.result();

                realms.put(Strings.MAP_REALMS, new HashMap<>(), put -> {
                    if (put.failed()) {
                        throw new RuntimeException(put.cause());
                    }
                });
            } else {
                throw new RuntimeException(cluster.cause());
            }
        });
    }

    public void getMetadataList(Future<ArrayList<RealmMetaData>> future) {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        realms.get(Strings.MAP_REALMS, map -> {

            if (map.succeeded()) {

                for (String key : map.result().keySet()) {
                    list.add(new RealmMetaData(map.result().get(key)));
                }

                future.complete(list);
            } else {
                future.fail(map.cause());
            }
        });
    }

    public void signToken(Future<Token> future, String realm, String domain) {
        realms.get(Strings.MAP_REALMS, map -> {
            RealmSettings settings = map.result().get(realm);

            if (map.succeeded()) {
                if (map.result() == null) {
                    future.fail(new RealmMissingException());
                } else {
                    future.complete(
                            new Token(
                                    new TokenFactory(getSecretBytes(settings)), domain));
                }
            } else {
                future.fail(map.cause());
            }
        });
    }

    private byte[] getSecretBytes(RealmSettings settings) {
        return settings.getAuthentication().getToken().getKey().getBytes();
    }

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

    public void put(RealmSettings realm) {
        realms.get(Strings.MAP_REALMS, map -> {
            HashMap<String, RealmSettings> list = map.result();

            list.replace(realm.getName(), realm);

            realms.replace(Strings.MAP_REALMS, list, put -> {
                if (put.failed()) {
                    throw new RuntimeException(put.cause());
                }
            });
        });
    }

    public void remove(String realmName) {
        realms.get(Strings.MAP_REALMS, get -> {
            HashMap<String, RealmSettings> map = get.result();

            map.remove(realmName);

            realms.replace(Strings.MAP_REALMS, map, put -> {
                if (put.failed())
                    throw new RuntimeException(put.cause());
            });
        });
    }

    public void update(String realmName, int players) {
        realms.get(Strings.MAP_REALMS, map -> {

            if (map.succeeded()) {
                map.result().get(realmName).setPlayers(players);

                realms.replace(Strings.MAP_REALMS, map.result(), put -> {
                    if (put.failed()) {
                        throw new RuntimeException(put.cause());
                    }
                });
            }
        });
    }
}

