package Game;

import Configuration.GameServerSettings;
import Configuration.RealmSettings;
import Game.Controller.Realm;
import Utilities.*;
import Configuration.Config;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         root game server, deploys realm servers.
 */
public class Server implements Verticle {
    private GameServerSettings settings;
    private Logger logger;
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.settings = Config.instance().getGameServerSettings();
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
