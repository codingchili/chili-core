package Authentication.Model;

import Configuration.RealmSettings;
import Protocol.Authentication.RealmMetaData;
import Utilities.Token;
import Utilities.TokenFactory;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Shares realmName data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class RealmStore {
    private LocalMap<String, RealmSettings> realms;

    public RealmStore(Vertx vertx) {
        realms = vertx.sharedData().getLocalMap("realms");
    }

    public ArrayList<RealmMetaData> getMetadataList() {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        for (RealmSettings realm : realms.values()) {
            list.add(new RealmMetaData(realm));
        }

        return list;
    }

    public Token signToken(String realm, String domain) throws RealmMissingException {
        return new Token(getTokenFactory(realm), domain);
    }

    private TokenFactory getTokenFactory(String realmName) throws RealmMissingException {
        RealmSettings realm = realms.get(realmName);

        if (realm == null) {
            throw new RealmMissingException();
        } else {
            return new TokenFactory(realm.getAuthentication().getToken().getKey().getBytes());

        }
    }

    public RealmSettings get(String realmName) throws RealmMissingException {
        RealmSettings realm = realms.get(realmName);

        if (realm == null) {
            throw new RealmMissingException();
        } else {
            return realm.removeAuthentication();
        }
    }

    public void put(RealmSettings realm) {
        realms.put(realm.getName(), realm);
    }

    public RealmSettings remove(String realmName) {
        return realms.remove(realmName);
    }
}

