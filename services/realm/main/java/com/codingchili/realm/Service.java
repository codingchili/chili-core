package com.codingchili.realm;

import com.codingchili.common.Strings;
import com.codingchili.realm.configuration.*;
import com.codingchili.realm.controller.RealmClientHandler;
import com.codingchili.realm.controller.RealmInstanceHandler;
import com.codingchili.realm.model.RealmNotUniqueException;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.WebsocketListener;

import static com.codingchili.core.context.FutureHelper.untyped;
import static com.codingchili.core.files.Configurations.system;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 * root game server, deploys realmName servers.
 */
public class Service implements CoreService {
    private RealmServerContext context;

    private static JsonObject getPing() {
        return new JsonObject()
                .put(Strings.PROTOCOL_ROUTE, Strings.ID_PING);
    }

    private static DeliveryOptions getDeliveryOptions() {
        return new DeliveryOptions()
                .setSendTimeout(system().getDeployTimeout());
    }

    @Override
    public void init(CoreContext core) {
        this.context = new RealmServerContext(core);
    }

    @Override
    public void start(Future<Void> start) {
        RealmServerSettings server = Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
        List<Future> deployments = new ArrayList<>();

        for (EnabledRealm enabled : server.getEnabled()) {
            RealmSettings realm = Configurations.get(enabled.getPath(), RealmSettings.class);
            realm.load(enabled.getInstances());
            Future<Void> future = Future.future();
            deploy(future, realm);
            deployments.add(future);
        }
        CompositeFuture.all(deployments).setHandler(untyped(start));
    }

    /**
     * Dynamically deploy a new realm, verifies that no existing nodes are already listening
     * on the same address by sending a ping.
     *
     * @param realm the realm to be deployed dynamically.
     */
    private void deploy(Future<Void> future, RealmSettings realm) {
        Consumer<RealmContext> deployer = (rc) -> {
            // Check if the routing id for the realm is unique
            context.bus().send(realm.getName(), getPing(), getDeliveryOptions(), response -> {

                if (response.failed()) {
                    // If no response then the id is not already in use.
                    ListenerSettings settings = rc.getListenerSettings();

                    CoreListener listener = new WebsocketListener()
                            .settings(() -> settings)
                            .handler(new RealmClientHandler(rc));

                    // deploy handler for incoming messages from instances.
                    rc.handler(() -> new RealmInstanceHandler(rc)).setHandler(instances -> {

                        if (instances.succeeded()) {
                            // deploy handler for incoming messages from clients.
                            rc.listener(() -> listener).setHandler(deploy -> {
                                if (deploy.failed()) {
                                    rc.onDeployRealmFailure(realm.getName());
                                    throw new RuntimeException(deploy.cause());
                                }
                            }).setHandler(clients -> {
                                if (clients.succeeded()) {
                                    future.complete();
                                } else {
                                    future.fail(clients.cause());
                                }
                            });
                        } else {
                            future.fail(instances.cause());
                        }
                    });

                } else {
                    future.fail(new RealmNotUniqueException());
                }
            });
        };

        // set up the realm context asynchronously.
        RealmContext.create(context, realm).setHandler(create -> {
            if (create.succeeded()) {
                deployer.accept(create.result());
            } else {
                future.fail(new RuntimeException(create.cause()));
            }
        });
    }
}
