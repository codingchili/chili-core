package Realm;

import Configuration.FileConfiguration;
import Protocols.ClusterVerticle;
import Realm.Configuration.RealmServerSettings;
import Realm.Configuration.RealmSettings;
import Realm.Controller.Realm;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Server extends ClusterVerticle {
    private RealmServerSettings settings;

    @Override
    public void init(Vertx vertx, Context context) {
        this.settings = FileConfiguration.instance().getGameServerSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {

        for (RealmSettings realm : settings.getRealms()) {
            vertx.deployVerticle(new Realm(settings, realm.load()));
        }

        logger.onServerStarted(start);
    }
}
