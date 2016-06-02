package Authentication.Controller;

import Protocols.AuthorizationHandler.Access;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.RealmStore;
import Authentication.Model.RealmMissingException;
import Configuration.Provider;
import Configuration.Authserver.AuthServerSettings;
import Configuration.Gameserver.RealmSettings;
import Game.Model.PlayerCharacter;
import Protocols.Game.CharacterRequest;
import Protocols.Game.CharacterResponse;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Protocols.Authentication.RealmRegister;
import Protocols.Authentication.RealmUpdate;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         Router used to authenticate realms and generate realmName lists.
 */
public class RealmHandler {
    private RealmStore realmStore;
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;

    public RealmHandler(AuthProvider provider) {
        this.accounts = provider.getAccountStore();
        this.settings = provider.getAuthserverSettings();
        this.realmStore = new RealmStore(provider.getVertx());

        provider.realmProtocol()
                .use(RealmUpdate.ACTION, this::update)
                .use(CharacterRequest.ACTION, this::character)
                .use(PacketHandler.CLOSE, this::disconnected)
                .use(RealmRequest.AUTHENTICATED, this::register, Access.PUBLIC);
    }

    private void register(RealmRequest request) {
        RealmSettings realm = request.realm();

        realm.setTrusted(settings.isPublicRealm(realm.getName()));
        realmStore.put(realm);
        request.write(new RealmRegister(true));
    }

    private void update(RealmRequest request) {
        String realmName = request.realmName();
        int players = request.players();

        try {
            RealmSettings realm = realmStore.get(realmName);
            realm.setPlayers(players);
            realmStore.put(realm);
        } catch (RealmMissingException e) {
            request.error();
        }
    }

    private void disconnected(RealmRequest request) {
        realmStore.remove(request.realm().getName());
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