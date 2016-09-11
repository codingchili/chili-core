package Authentication;

import Authentication.Model.AsyncRealmStore;
import Protocols.Authentication.RealmMetaData;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Realm.Configuration.RealmSettings;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 */
class RealmStoreMock implements AsyncRealmStore {
    private HashMap<String, RealmSettings> realms = new HashMap<>();

    @Override
    public void getMetadataList(Future<ArrayList<RealmMetaData>> future) {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        for (String key : realms.keySet()) {
            list.add(new RealmMetaData(realms.get(key)));
        }

        future.complete(list);
    }

    @Override
    public void signToken(Future<Token> future, String realmName, String domain) {
        RealmSettings realm = realms.get(realmName);

        future.complete(
                new Token(
                        new TokenFactory(realm.getAuthentication().getToken().getKey().getBytes()), domain));
    }

    @Override
    public void get(Future<RealmSettings> future, String realmName) {
        future.complete(realms.get(realmName));
    }

    @Override
    public void put(Future<Void> future, RealmSettings realm) {
        realms.put(realm.getName(), realm);
        future.complete();
    }

    @Override
    public void remove(Future<Void> future, String realmName) {
        realms.remove(realmName);
        future.complete();
    }

    @Override
    public void update(Future<Void> future, String realmName, int players) {
        realms.get(realmName).setPlayers(players);
        future.complete();
    }
}
