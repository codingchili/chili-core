package Authentication.Controller;

import Authentication.Configuration.AuthProvider;
import Authentication.Model.*;
import Logging.Model.Logger;
import Protocols.*;
import Protocols.Authentication.CharacterList;
import Protocols.Authentication.ClientAuthentication;
import Protocols.Authentication.RealmList;
import Protocols.Authentication.RealmMetaData;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Realm.Configuration.RealmSettings;
import Realm.Model.PlayerCharacter;
import Realm.Model.PlayerClass;
import io.vertx.core.Future;

import java.util.ArrayList;

import static Configuration.Strings.*;
import static Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 *         Routing used to authenticate users and create/delete characters.
 */
public class ClientHandler implements HandlerProvider {
    private Protocol protocol = new Protocol(this.getClass());
    private AsyncRealmStore realmStore;
    private AsyncAccountStore accounts;
    private TokenFactory tokens;
    private Logger logger;

    public ClientHandler(AuthProvider provider) {
        this.logger = provider.getLogger();
        this.accounts = provider.getAccountStore();
        this.tokens = new TokenFactory(provider.getAuthserverSettings().getClientSecret());
        this.realmStore = provider.getRealmStore();
    }

    @Authenticator
    public Access authenticate(Request request) {
        boolean authorized = tokens.verifyToken(request.token());
        return (authorized) ? Access.AUTHORIZE : Access.PUBLIC;
    }

    @Handler(CLIENT_REALM_TOKEN)
    public void realmtoken(ClientRequest request) {
        Future<Token> token = Future.future();

        token.setHandler(result -> {
            if (result.succeeded()) {
                request.write(result.result());
            } else {
                request.error();
            }
        });

        realmStore.signToken(token, request.realmName(), request.account());
    }

    @Handler(CLIENT_CHARACTER_LIST)
    public void characterList(ClientRequest request) {
        Future<ArrayList<PlayerCharacter>> characterFuture = Future.future();

        characterFuture.setHandler(characters -> {
            if (characters.succeeded()) {
                Future<RealmSettings> realmFuture = Future.future();

                realmFuture.setHandler(realm -> {
                    if (realm.succeeded()) {
                        request.write(new CharacterList(realm.result(), characters.result()));
                    } else {
                        request.error();
                    }
                });

                realmStore.get(realmFuture, request.realmName());
            } else {
                request.missing();
            }
        });
        accounts.findCharacters(characterFuture, request.realmName(), request.account());
    }

    @Handler(CLIENT_CHARACTER_CREATE)
    public void characterCreate(ClientRequest request) {
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
        Future<PlayerCharacter> templateFuture = Future.future();

        templateFuture.setHandler(template -> {

            if (template.succeeded()) {
                accounts.upsertCharacter(Future.future().setHandler(creation -> {
                    if (creation.succeeded()) {
                        request.accept();
                    } else {
                        request.unauthorized();
                    }
                }), request.realmName(), request.account(), template.result());
            } else {
                try {
                    throw template.cause();
                } catch (PlayerClassDisabledException | RealmMissingException e) {
                    request.missing();
                } catch (Throwable throwable) {
                    request.error();
                }
            }
        });
        createCharacterFromTemplate(templateFuture, request);
    }


    private void createCharacterFromTemplate(Future<PlayerCharacter> future, ClientRequest request) {
        Future<RealmSettings> realmFuture = Future.future();

        realmFuture.setHandler(realm -> {
            if (realm.succeeded()) {
                try {
                    future.complete(readTemplate(realm.result(), request.character(), request.className()));
                } catch (PlayerClassDisabledException e) {
                    future.fail(e);
                }
            } else {
                future.fail(realm.cause());
            }
        });
        realmStore.get(realmFuture, request.realmName());
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

    @Handler(CLIENT_CHARACTER_REMOVE)
    public void characterRemove(ClientRequest request) {
        accounts.removeCharacter(Future.future().setHandler(remove -> {
            if (remove.succeeded())
                request.accept();
            else
                request.error();
        }), request.realmName(), request.account(), request.character());
    }

    @Handler(value = CLIENT_REGISTER, access =  PUBLIC)
    public void register(ClientRequest request) {
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded()) {
                    sendAuthentication(result.result(), request, true);
                } else {
                    throw future.cause();
                }
            } catch (AccountExistsException e) {
                request.conflict();
            } catch (Throwable e) {
                request.error();
            }
        });
        accounts.register(future, request.getAccount());
    }

    @Handler(value = CLIENT_AUTHENTICATE, access = PUBLIC)
    public void authenticate(ClientRequest request) {
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
        Future<ArrayList<RealmMetaData>> future = Future.future();


        future.setHandler(metadata -> {
            request.authenticate(
                    new ClientAuthentication(
                            account,
                            new Token(tokens, account.getUsername()),
                            registered,
                            metadata.result()));

            if (registered)
                logger.onRegistered(account, request.sender());
            else
                logger.onAuthenticated(account, request.sender());
        });

        realmStore.getMetadataList(future);
    }

    @Handler(value = CLIENT_REALM_LIST, access = PUBLIC)
    public void realmlist(ClientRequest request) {
        Future<ArrayList<RealmMetaData>> future = Future.future();

        future.setHandler(result -> {
            request.write(new RealmList(result.result()));
        });

        realmStore.getMetadataList(future);
    }

    @Override
    public void process(Request request) throws AuthorizationRequiredException, HandlerMissingException {
        protocol.handle(this, request);
    }
}
