package Authentication.Controller;

import Authentication.Model.*;
import Configuration.AuthServerSettings;
import Configuration.Config;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Game.Model.PlayerClass;
import Protocol.Authentication.CharacterList;
import Protocol.Authentication.ClientAuthentication;
import Utilities.Logger;
import Utilities.Token;
import Utilities.TokenFactory;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Router used to authenticate users and create/delete characters.
 */
public class ClientHandler {
    private RealmHandler realms;
    private AsyncAccountStore accounts;
    private TokenFactory clientToken;
    private Logger logger;

    public ClientHandler(ClientProtocol protocol, RealmHandler realms, AsyncAccountStore accounts, Logger logger) {
        AuthServerSettings settings = Config.instance().getAuthSettings();
        this.logger = logger;
        this.clientToken = new TokenFactory(settings.getClientSecret());
        this.realms = realms;
        this.accounts = accounts;

        protocol.use(ClientProtocol.CHARACTERLIST, this::characterList)
                .use(ClientProtocol.CHARACTERCREATE, this::characterCreate)
                .use(ClientProtocol.CHARACTERREMOVE, this::characterRemove)
                .use(ClientProtocol.AUTHENTICATE, this::authenticate)
                .use(ClientProtocol.REGISTER, this::register)
                .use(ClientProtocol.REALMTOKEN, this::realmtoken)
                .use(ClientProtocol.REALMLIST, this::realmlist);
    }

    private void realmtoken(ClientRequest request) {
        if (verify(request))
            request.write(realms.signToken(request.realm(), request.account()));
    }

    private boolean verify(ClientRequest request) {
        boolean verified = clientToken.verifyToken(request.token());

        if (!verified)
            request.unauthorize();

        return verified;
    }


    private void characterList(ClientRequest request) {
        if (verify(request)) {
            Future<ArrayList<PlayerCharacter>> future = Future.future();

            future.setHandler(result -> {
                if (result.succeeded()) {
                    request.write(new CharacterList(realms.getRealm(request.realm()), result.result()));
                } else
                    request.missing();
            });
            accounts.findCharacters(future, request.realm(), request.account());
        }
    }

    private void characterCreate(ClientRequest request) {
        if (verify(request)) {
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

        } catch (PlayerClassDisabledException e) {
            request.missing();
        }
    }


    private PlayerCharacter createCharacterFromTemplate(ClientRequest request) throws PlayerClassDisabledException {
        return readTemplate(realms.getRealm(request.realm()), request.character(), request.className());
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
        if (verify(request)) {
            accounts.removeCharacter(Future.future().setHandler(remove -> {
                if (remove.succeeded())
                    request.accept();
                else
                    request.error();
            }), request.realm(), request.account(), request.character());
        }
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
        Token token = new Token(clientToken, account.getUsername());
        request.write(new ClientAuthentication(account, token, registered, realms.getMetadataList()));

        if (registered)
            logger.onRegistered(account, request.sender());
        else
            logger.onAuthenticated(account, request.sender());
    }

    private void realmlist(ClientRequest request) {
        request.write(realms.getMetadataList());
    }
}
