package Authentication.Model;

import Configuration.Strings;
import Protocols.Authentication.RealmMetaData;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Protocols.Serializer;
import Realm.Configuration.RealmSettings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Shares realmName data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class RealmStore {
    // A list is stored within the map so that it is possible to enumerate available realms.
    private AsyncMap<String, ArrayList<RealmSettings>> realms;

    public RealmStore(Vertx vertx) {
        vertx.sharedData().<String, ArrayList<RealmSettings>>getClusterWideMap(Strings.MAP_REALMS, cluster -> {
            if (cluster.succeeded()) {
                realms = cluster.result();

                realms.put(Strings.MAP_REALMS, new ArrayList<>(), put -> {
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

                for (RealmSettings realm : map.result()) {
                    list.add(new RealmMetaData(realm));
                }

                future.complete(list);
            } else {
                future.fail(map.cause());
            }
        });
    }

    public Token signToken(String realm, String domain) throws RealmMissingException {
        return new Token(getTokenFactory(realm), domain);
    }

    private TokenFactory getTokenFactory(String realmName) throws RealmMissingException {
        return new TokenFactory(find(realmName).getAuthentication().getToken().getKey().getBytes());
    }

    public RealmSettings get(String realmName) throws RealmMissingException {
        return find(realmName).removeAuthentication();
    }

    public void put(RealmSettings realm) {
        realms.get(Strings.MAP_REALMS, map -> {
            ArrayList<RealmSettings> list = map.result();

            if (list.contains(realm)) {
                list.remove(realm);
                list.add(realm);
            }

            realms.put(Strings.MAP_REALMS, list, put -> {
                if (put.failed()) {
                    throw new RuntimeException(put.cause());
                }
            });
        });
    }

    public void remove(String realmName) {
        realms.get(Strings.MAP_REALMS, map -> {
            ArrayList<RealmSettings> list = map.result();

            list.remove(new RealmSettings().setName(realmName));

            realms.put(Strings.MAP_REALMS, list, put -> {
                if (put.failed())
                    throw new RuntimeException(put.cause());
            });
        });
    }

    public void update(String realmName, int players) {
        realms.get(Strings.MAP_REALMS, map -> {
            boolean updated = false;

            for (RealmSettings settings : map.result()) {
                if (settings.getName().equals(realmName)) {
                    if (settings.getPlayers() != players) {
                        updated = true;
                        settings.setPlayers(players);
                    }
                }
            }

            if (updated) {
                realms.put(Strings.MAP_REALMS, map.result(), put -> {
                    if (put.failed()) {
                        throw new RuntimeException(put.cause());
                    }
                });
            }
        });
    }

    private void find(Future<RealmSettings> future, String realmName) {
        realms.get(Strings.MAP_REALMS, map -> {
            RealmSettings result = null;

            for (RealmSettings settings : map.result()) {
                if (settings.getName().equals(realmName)) {
                    result = settings;
                }
            }

            if (result == null) {
                future.fail(new RealmMissingException());
            } else {
                future.complete(result);
            }
        });
    }
}

