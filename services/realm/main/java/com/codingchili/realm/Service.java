package com.codingchili.realm;

import com.codingchili.common.Strings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.realm.configuration.*;
import com.codingchili.realm.controller.CharacterHandler;
import com.codingchili.realm.instance.context.InstanceContext;
import com.codingchili.realm.instance.context.InstanceSettings;
import com.codingchili.realm.instance.controller.InstanceHandler;
import com.codingchili.realm.model.RealmNotUniqueException;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static com.codingchili.core.context.FutureHelper.generic;
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
        CompositeFuture.all(deployments).setHandler(generic(start));
    }

    /**
     * Dynamically deploy a new realm, verifies that no existing nodes are already listening
     * on the same address by sending a ping.
     *
     * @param realm the realm to be deployed dynamically.
     */
    private void deploy(Future<Void> future, RealmSettings realm) {
        // Check if the routing id for the realm is unique
        context.bus().send(realm.getRemote(), getPing(), getDeliveryOptions(), response -> {

            if (response.failed()) {
                // If no response then the id is not already in use.
                    RealmContext realmContext = new RealmContext(context, realm);
                    CoreListener listener = new ClusterListener()
                            .handler(new CharacterHandler(realmContext));

                    realmContext.listener(() -> listener).setHandler(deploy -> {
                        if (deploy.failed()) {
                            realmContext.onDeployRealmFailure(realm.getName());
                            throw new RuntimeException(deploy.cause());
                        } else {
                            startInstances(future, realmContext);
                        }
                    });
            } else {
                future.fail(new RealmNotUniqueException());
            }
        });
    }

    private void startInstances(Future<Void> future, RealmContext context) {
        List<Future> futures = new ArrayList<>();
        for (InstanceSettings instance : context.instances()) {
            Future deploy = Future.future();
            futures.add(deploy);

            context.handler(() -> new InstanceHandler(new InstanceContext(context, instance))).setHandler((done) -> {
                if (done.succeeded()) {
                    deploy.complete();
                } else {
                    context.onInstanceFailed(instance.getName(), done.cause());
                    deploy.fail(done.cause());
                }
            });
        }
        CompositeFuture.all(futures).setHandler(done -> {
            if (done.succeeded()) {
                future.complete();
            } else {
                future.fail(done.cause());
            }
        });
    }
}
