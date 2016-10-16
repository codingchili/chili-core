package com.codingchili.core.Realm;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Configuration.System.VertxSettings;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Protocols.ClusterListener;
import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Realm.Configuration.EnabledRealm;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Controller.RealmHandler;
import com.codingchili.core.Realm.Model.RealmNotUniqueException;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

import static com.codingchili.core.Configuration.Strings.PATH_VERTX;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Server extends ClusterVerticle {
    private RealmServerSettings settings;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.settings = FileConfiguration.get(Strings.PATH_REALMSERVER, RealmServerSettings.class);
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws IOException {
        for (EnabledRealm enabled : settings.getEnabled()) {
            RealmSettings realm = FileConfiguration.get(enabled.getPath(), RealmSettings.class);
            realm.load(enabled.getInstances());
            Future<Void> future = Future.future();

            future.setHandler(result -> {
                if (result.failed()) {
                    logger.onDeployRealmFailure(realm);
                }
            });
            deploy(future, vertx, settings, realm);
        }

        logger.onServerStarted(start);
    }

    /**
     * Dynamically deploy a new Realm, verifies that no existing nodes are already listening
     * on the same address by sending a ping.
     *
     * @param settings realmserver settings to use for deployment.
     * @param realm    the realm to be deployed dynamically.
     */
    public static void deploy(Future future, Vertx vertx, RealmServerSettings settings, RealmSettings realm) {
        RealmProvider provider = new RealmProvider(vertx, settings, realm);

        // Check if the routing id for the realm is unique
        vertx.eventBus().send(realm.getRemote(), getPing(), getDeliveryOptions(), response -> {

            if (response.failed()) {
                // If no response then the id is not already in use.
                vertx.deployVerticle(new ClusterListener(new RealmHandler(provider)), deploy -> {
                    if (deploy.failed()) {
                        throw new RuntimeException(deploy.cause());
                    }
                });

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
        VertxSettings vertxSettings = FileConfiguration.get(PATH_VERTX, VertxSettings.class);

        return new DeliveryOptions()
                .setSendTimeout(vertxSettings.getDeployTimeout());
    }
}
