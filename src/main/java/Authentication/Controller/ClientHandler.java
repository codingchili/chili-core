package Authentication.Controller;

import Authentication.Configuration.AuthProvider;
import Authentication.Model.*;
import Configuration.Strings;
import Realm.Configuration.RealmSettings;
import Realm.Model.PlayerCharacter;
import Realm.Model.PlayerClass;
import Protocols.Authentication.CharacterList;
import Protocols.Authentication.ClientAuthentication;
import Protocols.Authentication.RealmList;
import Protocols.AuthorizationHandler.Access;
import Logging.Model.Logger;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Router used to authenticate users and create/delete characters.
 */
public class ClientHandler {
    private RealmStore realmStore;
    private AsyncAccountStore accounts;
    private TokenFactory factory;
    private Logger logger;

    public ClientHandler(AuthProvider provider) {
        this.logger = provider.getLogger();
        this.accounts = provider.getAccountStore();
        this.factory = new TokenFactory(provider.getAuthserverSettings().getClientSecret());
        this.realmStore = new RealmStore(provider.getVertx());

        provider.clientProtocol()
                .use(Strings.CLIENT_CHARACTER_LIST, this::characterList)
                .use(Strings.CLIENT_CHARACTER_CREATE, this::characterCreate)
                .use(Strings.CLIENT_CHARACTER_REMOVE, this::characterRemove)
                .use(Strings.CLIENT_REALM_TOKEN, this::realmtoken)
                .use(Strings.CLIENT_AUTHENTICATE, this::authenticate, Access.PUBLIC)
                .use(Strings.CLIENT_REGISTER, this::register, Access.PUBLIC)
                .use(Strings.CLIENT_REALM_LIST, this::realmlist, Access.PUBLIC);
    }

    private void realmtoken(ClientRequest request) {
        try {
            request.write(realmStore.signToken(request.realmName(), request.account()));
        } catch (RealmMissingException e) {
            request.error();
        }
    }

    private void characterList(ClientRequest request) {
        Future<ArrayList<PlayerCharacter>> future = Future.future();

        future.setHandler(result -> {
            if (result.succeeded()) {
                try {
                    RealmSettings realm = realmStore.get(request.realmName());
                    request.write(new CharacterList(realm, result.result()));
                } catch (RealmMissingException e) {
                    request.error();
                }
            } else
                request.missing();
        });
        accounts.findCharacters(future, request.realmName(), request.account());
    }

    private void characterCreate(ClientRequest request) {
        Future<PlayerCharacter> find = Future.future();

        find.setHandler(found -> {
            if (found.succeeded()) {
                request.conflict();
            } else {
                upsertCharacter(request);
            }
        });
        accounts.findCharacter(find, request.realmName(), request.account(), request.character());
    }

    private void upsertCharacter(ClientRequest request) {
        try {
            PlayerCharacter character = createCharacterFromTemplate(request);

            accounts.addCharacter(Future.future().setHandler(creation -> {
                if (creation.succeeded()) {
                    request.accept();
                } else {
                    request.unauthorized();
                }
            }), request.realmName(), request.account(), character);

        } catch (PlayerClassDisabledException | RealmMissingException e) {
            request.missing();
        }
    }


    private PlayerCharacter createCharacterFromTemplate(ClientRequest request) throws PlayerClassDisabledException, RealmMissingException {
        return readTemplate(realmStore.get(request.realmName()), request.character(), request.className());
    }

    private PlayerCharacter readTemplate(RealmSettings realm, String characterName, String className) throws PlayerClassDisabledException {
        boolean enabled = false;

        for (PlayerClass pc : realm.getClasses()) {
            if (pc.getName().equals(className))
                enabled = true;
        }

        if (enabled) {
            return new PlayerCharacter(realm.getTemplate(), characterName, className);
        } else
            throw new PlayerClassDisabledException();
    }


    private void characterRemove(ClientRequest request) {
        accounts.removeCharacter(Future.future().setHandler(remove -> {
            if (remove.succeeded())
                request.accept();
            else
                request.error();
        }), request.realmName(), request.account(), request.character());
    }

    private void register(ClientRequest request) {
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded()) {
                    sendAuthentication(result.result(), request, true);
                } else
                    throw future.cause();

            } catch (AccountExistsException e) {
                request.conflict();
            } catch (Throwable e) {
                request.error();
            }
        });
        accounts.register(future, request.getAccount());
    }

    private void authenticate(ClientRequest request) {
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded()) {
                    sendAuthentication(result.result(), request, false);
                } else
                    throw future.cause();

            } catch (AccountMissingException e) {
                request.missing();
            } catch (AccountPasswordException e) {
                logger.onAuthenticationFailure(request.getAccount(), request.sender());
                request.unauthorized();
            } catch (Throwable e) {
                request.error();
            }
        });
        accounts.authenticate(future, request.getAccount());
    }

    private void sendAuthentication(Account account, ClientRequest request, boolean registered) {
        request.authenticate(
                new ClientAuthentication(
                        account,
                        new Token(factory, account.getUsername()),
                        registered,
                        realmStore.getMetadataList()));

        if (registered)
            logger.onRegistered(account, request.sender());
        else
            logger.onAuthenticated(account, request.sender());
    }

    private void realmlist(ClientRequest request) {
        request.write(new RealmList(realmStore.getMetadataList()));
    }
}
