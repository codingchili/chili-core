package Realm.Controller;

import Logging.Model.Logger;
import Protocols.*;
import Protocols.Authorization.TokenFactory;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Realm.Configuration.InstanceSettings;
import Realm.Configuration.RealmProvider;
import Realm.Configuration.RealmServerSettings;
import Realm.Configuration.RealmSettings;
import Realm.Model.Connection;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.util.HashMap;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Handles traveling between instances.
 */
public class RealmHandler extends AbstractHandler {
    private static final int REALM_UPDATE = 6000;
    private HashMap<String, Connection> connections = new HashMap<>();
    private Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private Logger logger;
    private RealmSettings settings;
    private RealmServerSettings server;
    private TokenFactory tokenFactory;
    private Vertx vertx;


    public RealmHandler(RealmProvider provider) {
        super(NODE_REALM);

        logger = provider.getLogger();
        settings = provider.getRealm();
        server = provider.getServer();
        vertx = provider.getVertx();
        tokenFactory = new TokenFactory(settings.getAuthentication());

        startInstances();
        registerRealm();

        protocol.use(REALM_CHARACTER_REQUEST, this::characterRequest)
                .use(ANY, this::instanceHandler);
    }

    // todo should the handler really start the instances? should it be done by a (new) owner?
    private void startInstances() {
        for (InstanceSettings instance : settings.getInstance()) {
            vertx.deployVerticle(new InstanceHandler(server, settings, instance));
        }
    }

    // todo the realms must be registered/unregistered with the authentication server.
    private void registerRealm() {
    }

    private Access authenticator(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            return Access.AUTHORIZED;
        } else {
            return Access.PUBLIC;
        }
    }

    private void instanceHandler(RealmRequest request) {
        request.missing();
    }

    private void characterRequest(RealmRequest request) {
        pipe(upstream(request.characterRequest()), request);
    }

    private void pipe(EventBus bus, RealmRequest request) {
        bus.start(handler -> {
            if (handler.succeeded()) {
                request.write(handler.result());
            } else {
                request.error();
            }
        });
    }

    private EventBus upstream(Object message) {
        return vertx.eventBus().send(NODE_AUTHENTICATION_REALMS, message);
    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(authenticator(request), request.action()).handle((RealmRequest) request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
            logger.onHandlerMissing(request.action());
        }
    }
}
