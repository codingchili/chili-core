package com.codingchili.services.Realm;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

import com.codingchili.core.Context.Deploy;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Protocol.ClusterNode;

import com.codingchili.services.Realm.Configuration.*;
import com.codingchili.services.Realm.Controller.RealmHandler;
import com.codingchili.services.Realm.Model.RealmNotUniqueException;
import com.codingchili.services.Shared.Strings;

import static com.codingchili.core.Files.Configurations.system;
import static com.codingchili.services.Realm.Configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Service extends ClusterNode {

    @Override
    public void start(Future<Void> start) throws IOException {
        RealmServerSettings server = Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);

        for (EnabledRealm enabled : server.getEnabled()) {
            RealmSettings realm = Configurations.get(enabled.getPath(), RealmSettings.class);
            realm.load(enabled.getInstances());
            Future<Void> future = Future.future();
            deploy(future, realm);
        }

        start.complete();
    }

    /**
     * Dynamically deploy a new Realm, verifies that no existing nodes are already listening
     * on the same address by sending a ping.
     *
     * @param realm    the realm to be deployed dynamically.
     */
    private void deploy(Future future, RealmSettings realm) {
        // Check if the routing id for the realm is unique
        vertx.eventBus().send(realm.getRemote(), getPing(), getDeliveryOptions(), response -> {

            if (response.failed()) {
                // If no response then the id is not already in use.
                Future<RealmContext> providerFuture = Future.future();

                providerFuture.setHandler(provider -> {
                    Deploy.service(new RealmHandler<>(provider.result()), deploy -> {
                        if (deploy.failed()) {
                            provider.result().onDeployRealmFailure(realm.getName());
                            throw new RuntimeException(deploy.cause());
                        }
                    });
                });

                RealmContext.create(providerFuture, realm, vertx);
            } else {
                future.fail(new RealmNotUniqueException());
            }
        });
    }

    private static JsonObject getPing() {
        return new JsonObject()
                .put(Strings.ID_ACTION, Strings.ID_PING);
    }

    private static DeliveryOptions getDeliveryOptions() {
        return new DeliveryOptions()
                .setSendTimeout(system().getDeployTimeout());
    }
}
