package Authentication.Controller;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.AsyncRealmStore;
import Protocols.*;
import Protocols.Authentication.RealmRegister;
import Protocols.Authorization.TokenFactory;
import Protocols.Realm.CharacterResponse;
import Realm.Configuration.RealmSettings;
import Realm.Model.PlayerCharacter;
import io.vertx.core.Future;

import static Configuration.Strings.*;
import static Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 *         Routing used to authenticate realms and generate realmName lists.
 */
public class RealmHandler extends HandlerProvider {
    private AsyncRealmStore realmStore;
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;
    private TokenFactory tokens;

    public RealmHandler(AuthProvider provider) {
        super(ClientHandler.class, provider.getLogger(),ADDRESS_AUTHENTICATION_REALMS);

        accounts = provider.getAccountStore();
        settings = provider.getAuthserverSettings();
        realmStore = provider.getRealmStore();
        tokens = provider.getClientTokenFactory();
    }

    @Authenticator
    public Access authenticate(Request request) {
        boolean authorized = tokens.verifyToken(request.token());
        return (authorized) ? Access.AUTHORIZE : Access.PUBLIC;
    }

    @Handles(value = REALM_AUTHENTICATE, access = PUBLIC)
    public void register(RealmRequest request) {
        Future<Void> realmFuture = Future.future();
        RealmSettings realm = request.realm();

        realm.setTrusted(settings.isTrustedRealm(realm.getName()));

        realmFuture.setHandler(insert -> {
            if (insert.succeeded()) {
                request.write(new RealmRegister(true));
            } else {
                request.error();
            }
        });

        realmStore.put(realmFuture, realm);
    }

    @Handles(REALM_UPDATE)
    public void update(RealmRequest request) {
        Future<Void> updateFuture = Future.future();
        String realmName = request.realmName();
        int players = request.players();

        updateFuture.setHandler(update -> {
            if (update.succeeded()) {
                request.write(new RealmRegister(true));
            } else {
                request.error();
            }
        });

        realmStore.update(updateFuture, realmName, players);
    }

    @Handles(CLIENT_CLOSE)
    public void disconnected(RealmRequest request) {
        Future<Void> realmFuture = Future.future();

        realmFuture.setHandler(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error();
            }
        });

        realmStore.remove(realmFuture, request.realm().getName());
    }

    @Handles(REALM_CHARACTER_REQUEST)
    public void character(RealmRequest request) {
        Future<PlayerCharacter> find = Future.future();

        find.setHandler(result -> {
            if (result.succeeded()) {
                request.write(new CharacterResponse(result.result(), request.sender()));
            } else
                request.error();
        });
        accounts.findCharacter(find, request.realmName(), request.account(), request.name());
    }
}