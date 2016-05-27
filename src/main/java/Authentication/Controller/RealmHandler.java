package Authentication.Controller;

import Authentication.Model.AsyncAccountStore;
import Authentication.Model.RealmMissingException;
import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Protocol.Game.CharacterRequest;
import Protocol.Game.CharacterResponse;
import Protocol.RealmRegister;
import Protocol.RealmUpdate;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         Router used to authenticate realms and generate realm lists.
 */
public class RealmHandler {
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;

    public RealmHandler(Provider store) {
        this.accounts = store.getAccountStore();
        this.settings = store.getAuthserverSettings();

        store.realmProtocol(Access.AUTHORIZE)
                .use(RealmRegister.ACTION, this::register)
                .use(RealmUpdate.ACTION, this::update)
                .use(CharacterRequest.ACTION, this::character)
                .use(RealmProtocol.CLOSE, this::disconnected);
    }

    private void register(RealmRequest request) {
        RealmSettings realm = request.realm();

        realm.setTrusted(settings.isPublicRealm(realm.getName()));
        RealmKeeper.put(realm);
        request.write(new RealmRegister(true));
    }

    private void update(RealmRequest request) {
        String realmName = request.realmName();
        int players = request.realm().getPlayers();
        RealmSettings realm;

        try {
            realm = RealmKeeper.get(realmName);
            realm.setPlayers(players);
            RealmKeeper.put(realm);
        } catch (RealmMissingException e) {
            request.error();
        }
    }

    private void disconnected(RealmRequest request) {
        RealmKeeper.remove(request.realm().getName());
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