package Realm;

import Configuration.FileConfiguration;
import Protocols.ClusterListener;
import Protocols.ClusterVerticle;
import Realm.Configuration.RealmProvider;
import Realm.Configuration.RealmServerSettings;
import Realm.Configuration.RealmSettings;
import Realm.Controller.RealmHandler;
import Logging.Model.DefaultLogger;
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
