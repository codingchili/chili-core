package com.codingchili.core.Realm.Controller;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Access;
import com.codingchili.core.Protocols.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocols.Exception.HandlerMissingException;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Controller.InstanceHandler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;


import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Handles traveling between instances.
 */
public class RealmHandler extends AbstractHandler {
    private Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private Logger logger;
    private RealmSettings settings;
    private RealmServerSettings server;
    private TokenFactory tokenFactory;
    private Vertx vertx;


    public RealmHandler(RealmProvider provider) {
        super(provider.getRealm().getRemote());

        logger = provider.getLogger();
        settings = provider.getRealm();
        server = provider.getServer();
        vertx = provider.getVertx();
        tokenFactory = new TokenFactory(settings.getAuthentication());

        startInstances();
        registerRealm();

        protocol.use(Strings.REALM_PING, this::ping, Access.PUBLIC)
                .use(REALM_CHARACTER_REQUEST, this::characterRequest)
                .use(ANY, this::instanceHandler);
    }

    private void startInstances() {
        for (InstanceSettings instance : settings.getInstance()) {
            vertx.deployVerticle(new InstanceHandler(server, settings, instance));
        }
    }

    private void ping(RealmRequest request) {
        request.accept();
    }

    private void registerRealm() {
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

    private Access authenticator(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            return Access.AUTHORIZED;
        } else {
            return Access.PUBLIC;
        }
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
