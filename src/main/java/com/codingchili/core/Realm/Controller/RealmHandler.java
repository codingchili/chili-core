package com.codingchili.core.Realm.Controller;

import com.codingchili.core.Protocols.*;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Configuration.InstanceProvider;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.core.Realm.Instance.Controller.InstanceHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Handles traveling between instances.
 */
public class RealmHandler extends AbstractHandler {
    private final Protocol<RequestHandler<RealmRequest>> protocol = new Protocol<>();
    private final RealmSettings realm;
    private final TokenFactory tokenFactory;
    private final Vertx vertx;


    public RealmHandler(RealmProvider provider) {
        super(provider.getRealm().getRemote());

        logger = provider.getLogger();
        realm = provider.getRealm();
        vertx = provider.getVertx();
        tokenFactory = new TokenFactory(realm.getAuthentication());

        startInstances();
        registerRealm();

        protocol.use(REALM_CHARACTER_REQUEST, this::characterRequest)
                .use(ANY, this::instanceHandler)
                .use(ID_PING, this::ping, Access.PUBLIC);
    }

    private void startInstances() {
        for (InstanceSettings instance : realm.getInstance()) {
            InstanceProvider provider = new InstanceProvider(realm, instance, vertx);
            vertx.deployVerticle(new ClusterListener(new InstanceHandler(provider)));
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
    public void handle(Request request) throws ProtocolException {
        protocol.get(authenticator(request), request.action()).handle(new RealmRequest(request));
    }

    @Override
    public void stop(Future<Void> future) {
        logger.onRealmStopped(future, realm);
    }

    @Override
    public void start(Future<Void> future) {
        logger.onRealmStarted(realm);
        future.complete();
    }
}
