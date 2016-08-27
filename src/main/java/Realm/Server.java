package Realm;

import Configuration.FileConfiguration;
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
public class Server implements Verticle {
    private RealmServerSettings settings;
    private Logger logger;
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.settings = FileConfiguration.instance().getGameServerSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {

        for (RealmSettings realm : settings.getRealms()) {
            vertx.deployVerticle(new Realm(settings, realm.load()));
        }

        logger.onServerStarted();
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
