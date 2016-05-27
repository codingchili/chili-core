package Authentication.Controller;

import Authentication.Model.*;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Game.Model.PlayerClass;
import Protocol.Authentication.CharacterList;
import Protocol.Authentication.ClientAuthentication;
import Protocol.Authentication.RealmList;
import Utilities.Logger;
import Utilities.Token;
import Utilities.TokenFactory;
import io.vertx.core.Future;
import Authentication.Model.AuthorizationHandler.Access;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Router used to authenticate users and create/delete characters.
 */
public class ClientHandler {
    private AsyncAccountStore accounts;
    private TokenFactory factory;
    private Logger logger;

    public ClientHandler(Provider provider) {
        this.logger = provider.getLogger();
        this.accounts = provider.getAccountStore();
        this.factory = new TokenFactory(provider.getAuthserverSettings().getClientSecret());

        provider.clientProtocol(Access.AUTHORIZE)
                .use(ClientProtocol.CHARACTERLIST, this::characterList)
                .use(ClientProtocol.CHARACTERCREATE, this::characterCreate)
                .use(ClientProtocol.CHARACTERREMOVE, this::characterRemove)
                .use(ClientProtocol.REALMTOKEN, this::realmtoken)
                .use(ClientProtocol.AUTHENTICATE, this::authenticate, Access.PUBLIC)
                .use(ClientProtocol.REGISTER, this::register, Access.PUBLIC)
                .use(ClientProtocol.REALMLIST, this::realmlist, Access.PUBLIC);
    }

    private void realmtoken(ClientRequest request) {
        request.write(RealmKeeper.signToken(request.realm(), request.account()));
    }

    private void characterList(ClientRequest request) {
        Future<ArrayList<PlayerCharacter>> future = Future.future();

        future.setHandler(result -> {
            if (result.succeeded()) {
                try {
                    RealmSettings realm = RealmKeeper.get(request.realm());
                    request.write(new CharacterList(realm, result.result()));
                } catch (RealmMissingException e) {
                    request.error();
                }
            } else
                request.missing();
        });
        accounts.findCharacters(future, request.realm(), request.account());
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
        accounts.findCharacter(find, request.realm(), request.account(), request.character());
    }

    private void upsertCharacter(ClientRequest request) {
        try {
            PlayerCharacter character = createCharacterFromTemplate(request);

            accounts.addCharacter(Future.future().setHandler(creation -> {
                if (creation.succeeded()) {
                    request.accept();
                } else {
                    request.unauthorize();
                }
            }), request.realm(), request.account(), character);

        } catch (PlayerClassDisabledException | RealmMissingException e) {
            request.missing();
        }
    }


    private PlayerCharacter createCharacterFromTemplate(ClientRequest request) throws PlayerClassDisabledException, RealmMissingException {
        return readTemplate(RealmKeeper.get(request.realm()), request.character(), request.className());
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
        }), request.realm(), request.account(), request.character());
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
                request.unauthorize();
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
                        RealmKeeper.getMetadataList()));

        if (registered)
            logger.onRegistered(account, request.sender());
        else
            logger.onAuthenticated(account, request.sender());
    }

    private void realmlist(ClientRequest request) {
        request.write(new RealmList(RealmKeeper.getMetadataList()));
    }
}
