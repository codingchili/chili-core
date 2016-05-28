package Authentication.Controller;

import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.RealmStore;
import Authentication.Model.RealmMissingException;
import Authentication.Model.Provider;
import Configuration.AuthServerSettings;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Protocol.Game.CharacterRequest;
import Protocol.Game.CharacterResponse;
import Protocol.RealmRegister;
import Protocol.RealmUpdate;
import Utilities.TokenFactory;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         Router used to authenticate realms and generate realmName lists.
 */
public class RealmHandler {
    private RealmStore realmStore;
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;
    private TokenFactory tokens;

    public RealmHandler(Provider provider) {
        this.accounts = provider.getAccountStore();
        this.settings = provider.getAuthserverSettings();
        this.tokens = new TokenFactory(settings.getRealmSecret());
        this.realmStore = new RealmStore(provider.getVertx());

        new Protocol<PacketHandler<RealmRequest>>(Access.AUTHORIZE)
                .use(RealmUpdate.ACTION, this::update)
                .use(CharacterRequest.ACTION, this::character)
                .use(Protocol.CLOSE, this::disconnected)
                .use(RealmRegister.ACTION, this::register, Access.PUBLIC)
                .use(Protocol.AUTHENTICATE, this::authenticate, Access.PUBLIC);
    }

    private void authenticate(RealmRequest request) {
        if (tokens.verifyToken(request.token())) {
            request.connection().authenticate(request.token().getDomain());
            request.accept();
        } else {
            request.error();
        }
    }

    private void register(RealmRequest request) {
        RealmSettings realm = request.realm();

        realm.setTrusted(settings.isPublicRealm(realm.getName()));
        realmStore.put(realm);
        request.write(new RealmRegister(true));
    }

    private void update(RealmRequest request) {
        String realmName = request.realmName();
        int players = request.realm().getPlayers();
        RealmSettings realm;

        try {
            realm = realmStore.get(realmName);
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