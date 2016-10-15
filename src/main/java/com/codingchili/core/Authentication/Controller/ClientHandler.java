package com.codingchili.core.Authentication.Controller;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Model.*;
import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Access;
import com.codingchili.core.Protocols.Authentication.CharacterList;
import com.codingchili.core.Protocols.Authentication.ClientAuthentication;
import com.codingchili.core.Protocols.Authentication.RealmList;
import com.codingchili.core.Protocols.Authentication.RealmMetaData;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.core.Realm.Instance.Model.PlayerClass;
import io.vertx.core.Future;

import java.util.ArrayList;

import static com.codingchili.core.Configuration.Strings.*;
import static com.codingchili.core.Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 *         Routing used to authenticate users and create/delete characters.
 */
public class ClientHandler extends AbstractHandler {
    private Protocol<RequestHandler<ClientRequest>> protocol = new Protocol<>();
    private AsyncRealmStore realmStore;
    private AsyncAccountStore accounts;
    private TokenFactory tokens;

    public ClientHandler(AuthProvider provider) {
        super(NODE_AUTHENTICATION_CLIENTS);

        logger = provider.getLogger();
        accounts = provider.getAccountStore();
        tokens = provider.getClientTokenFactory();
        realmStore = provider.getRealmStore();

        protocol.use(CLIENT_REALM_TOKEN, this::realmToken)
                .use(CLIENT_CHARACTER_LIST, this::characterList)
                .use(CLIENT_CHARACTER_CREATE, this::characterCreate)
                .use(CLIENT_CHARACTER_REMOVE, this::characterRemove)
                .use(CLIENT_REALM_LIST, this::realmlist, PUBLIC)
                .use(CLIENT_REGISTER, this::register, PUBLIC)
                .use(CLIENT_AUTHENTICATE, this::authenticate, PUBLIC)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    private Access authenticate(Request request) {
        boolean authorized = tokens.verifyToken(request.token());
        return (authorized) ? Access.AUTHORIZED : PUBLIC;
    }

    @Override
    public void handle(Request request) throws ProtocolException {
        protocol.get(authenticate(request), request.action()).handle(new ClientRequest(request));
    }

    private void realmToken(ClientRequest request) {
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

    private void characterList(ClientRequest request) {
        Future<ArrayList<PlayerCharacter>> characterFuture = Future.future();

        characterFuture.setHandler(characters -> {
            if (characters.succeeded()) {
                Future<RealmSettings> realmFuture = Future.future();

                realmFuture.setHandler(realm -> {
                    if (realm.result() != null) {
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
                request.error();
            }
        });
        createCharacterFromTemplate(templateFuture, request);
    }


    private void createCharacterFromTemplate(Future<PlayerCharacter> future, ClientRequest request) {
        Future<RealmSettings> realmFuture = Future.future();

        realmFuture.setHandler(realm -> {
            if (realm.result() != null) {
                try {
                    future.complete(readTemplate(realm.result(), request.character(), request.className()));
                } catch (PlayerClassDisabledException e) {
                    future.fail(e);
                }
            } else {
                future.fail(new RealmMissingException());
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

    private void characterRemove(ClientRequest request) {
        Future<PlayerCharacter> future = Future.future();

        future.setHandler(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error();
            }
        });

        accounts.removeCharacter(future, request.realmName(), request.account(), request.character());
    }

    private void register(ClientRequest request) {
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
        Future<ArrayList<RealmMetaData>> future = Future.future();


        future.setHandler(metadata -> {
            request.write(
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

    private void realmlist(Request request) {
        Future<ArrayList<RealmMetaData>> future = Future.future();

        future.setHandler(result -> request.write(new RealmList(result.result())));

        realmStore.getMetadataList(future);
    }
}
