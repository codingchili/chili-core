package Authentication.Controller;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.AsyncRealmStore;
import Configuration.Strings;
import Protocols.Authentication.RealmRegister;
import Protocols.Authentication.RealmUpdate;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Protocols.Realm.CharacterRequest;
import Protocols.Realm.CharacterResponse;
import Realm.Configuration.RealmSettings;
import Realm.Model.PlayerCharacter;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         Routing used to authenticate realms and generate realmName lists.
 */
public class RealmHandler {
    private AsyncRealmStore realmStore;
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;

    public RealmHandler(AuthProvider provider) {
        this.accounts = provider.getAccountStore();
        this.settings = provider.getAuthserverSettings();
        this.realmStore = provider.getRealmStore();

        apply(provider.realmProtocol());
    }

    public Protocol apply(Protocol<PacketHandler<RealmRequest>> protocol) {
        return protocol
                .use(RealmUpdate.ACTION, this::update)
                .use(CharacterRequest.ACTION, this::character)
                .use(Strings.CLIENT_CLOSE, this::disconnected)
                .use(Strings.REALM_AUTHENTICATE, this::register);
    }

    private void register(RealmRequest request) {
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

    private void update(RealmRequest request) {
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

    private void disconnected(RealmRequest request) {
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

    private void character(RealmRequest request) {
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