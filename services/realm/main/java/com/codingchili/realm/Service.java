package com.codingchili.realm;

import com.codingchili.common.Strings;
import com.codingchili.core.context.*;
import com.codingchili.realm.configuration.*;
import com.codingchili.realm.controller.CharacterHandler;
import com.codingchili.realm.model.RealmNotUniqueException;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreService;

import static com.codingchili.core.files.Configurations.system;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Service implements CoreService {
    private RealmServerContext context;

    @Override
    public void init(CoreContext core) {
        this.context = new RealmServerContext(core);
    }

    @Override
    public void stop(Future<Void> stop) {
        context.logger().onServiceStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        RealmServerSettings server = Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);

        for (EnabledRealm enabled : server.getEnabled()) {
            RealmSettings realm = Configurations.get(enabled.getPath(), RealmSettings.class);
            realm.load(enabled.getInstances());
            Future<Void> future = Future.future();
            deploy(future, realm);
        }
        context.logger().onServiceStarted(start);
    }

    /**
     * Dynamically deploy a new realm, verifies that no existing nodes are already listening
     * on the same address by sending a ping.
     *
     * @param realm    the realm to be deployed dynamically.
     */
    private void deploy(Future future, RealmSettings realm) {
        // Check if the routing id for the realm is unique
        context.bus().send(realm.getRemote(), getPing(), getDeliveryOptions(), response -> {

            if (response.failed()) {
                // If no response then the id is not already in use.
                Future<RealmContext> providerFuture = Future.future();

                providerFuture.setHandler(provider -> {
                    RealmContext realmContext = provider.result();

                    realmContext.handler(new CharacterHandler(realmContext), deploy -> {
                        if (deploy.failed()) {
                            provider.result().onDeployRealmFailure(realm.getName());
                            throw new RuntimeException(deploy.cause());
                        }
                    });
                });

                RealmContext.create(providerFuture, realm, context);
            } else {
                future.fail(new RealmNotUniqueException());
            }
        });
    }

    private static JsonObject getPing() {
        return new JsonObject()
                .put(Strings.PROTOCOL_ROUTE, Strings.ID_PING);
    }

    private static DeliveryOptions getDeliveryOptions() {
        return new DeliveryOptions()
                .setSendTimeout(system().getDeployTimeout());
    }
}
