package com.codingchili.core.Realm;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Protocols.ClusterListener;
import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Realm.Configuration.RealmProvider;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Controller.RealmHandler;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Server extends ClusterVerticle {
    private RealmServerSettings settings;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.settings = FileConfiguration.instance().getGameServerSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        for (RealmSettings realm : settings.getRealms()) {
            RealmProvider provider = new RealmProvider(vertx, settings, realm);
            vertx.deployVerticle(new ClusterListener(new RealmHandler(provider)));
        }

        logger.onServerStarted(start);
    }
}
