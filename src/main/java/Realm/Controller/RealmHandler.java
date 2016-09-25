package Realm.Controller;

import Configuration.Strings;
import Protocols.Access;
import Protocols.Authenticator;
import Protocols.Authorization.TokenFactory;
import Protocols.HandlerProvider;
import Protocols.Handles;
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
public class RealmHandler extends HandlerProvider {
    private static final int REALM_UPDATE = 6000;
    private HashMap<String, Connection> connections = new HashMap<>();
    private RealmSettings settings;
    private RealmServerSettings server;
    private TokenFactory tokenFactory;
    private Vertx vertx;


    public RealmHandler(RealmProvider provider) {
        super(RealmHandler.class, provider.getLogger(), Strings.NODE_REALM);
        this.settings = provider.getRealm();
        this.server = provider.getServer();
        this.vertx = provider.getVertx();
        this.tokenFactory = new TokenFactory(settings.getAuthentication());

        startInstances();
        registerRealm();
    }

    private void startInstances() {
        for (InstanceSettings instance : settings.getInstance()) {
            vertx.deployVerticle(new InstanceHandler(server, settings, instance));
        }
    }

    // todo register realm to authserver..
    private void registerRealm() {

    }

    @Authenticator
    public Access authenticator(RealmRequest request) {
        if (tokenFactory.verifyToken(request.token())) {
            return Access.AUTHORIZED;
        } else {
            return Access.PUBLIC;
        }
    }

    @Handles(ANY)
    public void instanceHandler(InstanceRequest request) {
        request.missing();
    }

    @Handles(REALM_CHARACTER_REQUEST)
    public void characterRequest(RealmRequest request) {
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
}
