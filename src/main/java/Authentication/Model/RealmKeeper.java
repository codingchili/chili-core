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
 *         Shares realm data between the clienthandler and the realmhandler.
 *         Allows the deployment of multiple handlers.
 */
public class RealmKeeper {
    private static LocalMap<String, RealmSettings> realms;

    public RealmKeeper(Vertx vertx) {
        realms = vertx.sharedData().getLocalMap("realms");
    }

    public static ArrayList<RealmMetaData> getMetadataList() {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        for (RealmSettings realm : realms.values()) {
            list.add(new RealmMetaData(realm));
        }

        return list;
    }

    public static Token signToken(String realm, String domain) {
        return new Token(getTokenFactory(realm), domain);
    }

    private static TokenFactory getTokenFactory(String realm) {
        return new TokenFactory(realms.get(realm).getAuthentication().getToken().getKey().getBytes());
    }

    public static RealmSettings get(String realmName) throws RealmMissingException {
        RealmSettings realm = realms.get(realmName);

        if (realm == null) {
            throw new RealmMissingException();
        } else {
            return realm.removeAuthentication();
        }
    }

    public static void put(RealmSettings realm) {
        realms.put(realm.getName(), realm);
    }

    public static RealmSettings remove(String realmName) {
        return realms.remove(realmName);
    }
}

