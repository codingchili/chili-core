package Authentication.Model;

import Configuration.Gameserver.RealmSettings;
import Protocols.Authentication.RealmMetaData;
import Protocols.Serializer;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
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
    private LocalMap<String, String> realms;

    public RealmStore(Vertx vertx) {
        realms = vertx.sharedData().getLocalMap("realms");
    }

    public ArrayList<RealmMetaData> getMetadataList() {
        ArrayList<RealmMetaData> list = new ArrayList<>();

        for (String realm : realms.values()) {
            list.add(new RealmMetaData(toRealm(realm)));
        }

        return list;
    }

    public Token signToken(String realm, String domain) throws RealmMissingException {
        return new Token(getTokenFactory(realm), domain);
    }

    private TokenFactory getTokenFactory(String realmName) throws RealmMissingException {
        if (realms.get(realmName) == null) {
            throw new RealmMissingException();
        } else {
            RealmSettings realm = toRealm(realms.get(realmName));
            return new TokenFactory(realm.getAuthentication().getToken().getKey().getBytes());

        }
    }

    public RealmSettings get(String realmName) throws RealmMissingException {
        String data = realms.get(realmName);

        if (data == null) {
            throw new RealmMissingException();
        } else {
            return ((RealmSettings) Serializer.unpack(realms.get(realmName), RealmSettings.class)).removeAuthentication();
        }
    }

    public void put(RealmSettings realm) {
        realms.put(realm.getName(), Serializer.pack(realm));
    }

    public RealmSettings remove(String realmName) {
        return toRealm(realms.remove(realmName));
    }

    private RealmSettings toRealm(String realm) {
        return (RealmSettings) Serializer.unpack(realm, RealmSettings.class);
    }
}

