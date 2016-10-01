package Authentication.Controller;

import Authentication.Configuration.AuthProvider;
import Authentication.Configuration.AuthServerSettings;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.AsyncRealmStore;
import Logging.Model.Logger;
import Protocols.*;
import Protocols.Authentication.RealmRegister;
import Protocols.Util.Protocol;
import Protocols.Util.TokenFactory;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Protocols.Realm.CharacterResponse;
import Realm.Configuration.RealmSettings;
import Realm.Model.PlayerCharacter;
import io.vertx.core.Future;

import static Configuration.Strings.*;
import static Protocols.Access.AUTHORIZED;
import static Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 *         Routing used to authenticate realms and generate realmName lists.
 */
public class RealmHandler extends AbstractHandler {
    private Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private AsyncRealmStore realmStore;
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;
    private Logger logger;
    private TokenFactory tokens;

    public RealmHandler(AuthProvider provider) {
        super(NODE_AUTHENTICATION_REALMS);

        logger = provider.getLogger();
        accounts = provider.getAccountStore();
        settings = provider.getAuthserverSettings();
        realmStore = provider.getRealmStore();
        tokens = provider.getClientTokenFactory();

        protocol.use(REALM_REGISTER, this::register, PUBLIC)
                .use(REALM_UPDATE, this::update)
                .use(CLIENT_CLOSE, this::disconnected)
                .use(REALM_CHARACTER_REQUEST, this::character);
    }

    private Access authenticate(Request request) {
        boolean authorized = tokens.verifyToken(request.token());
        return (authorized) ? AUTHORIZED : PUBLIC;
    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(authenticate(request), request.action()).handle((RealmRequest) request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
            logger.onHandlerMissing(request.action());
        }
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