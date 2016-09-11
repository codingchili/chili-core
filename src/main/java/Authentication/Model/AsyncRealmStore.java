package Authentication.Model;

import Protocols.Authentication.RealmMetaData;
import Protocols.Authorization.Token;
import Realm.Configuration.RealmSettings;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *
 * Asynchronous cluster-wide realm store.
 */
public interface AsyncRealmStore {

    /**
     * Assemble a list of metadata for all available realms.
     */
    void getMetadataList(Future<ArrayList<RealmMetaData>> future);

    /**
     * Sign an user authentication token with a realms secret.
     * @param realmName name of the realm that should sign the token.
     * @param domain the domain (username) in which the token is valid.
     */
    void signToken(Future<Token> future, String realmName, String domain);

    /**
     * Get all information available about a realm.
     * @param realmName name of the realm to find.
     */
    void get(Future<RealmSettings> future, String realmName);

    /**
     * Place a realm into the cluster-wide map.
     * @param realm realm information to be inserted.
     */
    void put(Future<Void> future, RealmSettings realm);

    /**
     * Removes a realm from the cluster-wide map.
     * @param realmName name of the realm.
     */
    void remove(Future<Void> future, String realmName);

    /**
     * Update an existing realm with new information.
     * @param realmName name of the realm to be updated.
     * @param players the current number of players in the realm.
     */
    void update(Future<Void> future, String realmName, int players);
}
