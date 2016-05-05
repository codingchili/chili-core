package Game;

import Configuration.GameServerSettings;
import Configuration.InstanceSettings;
import Configuration.RealmSettings;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Created by Robin on 2016-04-27.
 * <p>
 * Handles players in a world.
 */
public class Instance implements Verticle {
    private Vertx vertx;
    private InstanceSettings settings;
    private RealmSettings realm;
    private GameServerSettings game;
    private Logger logger;

    public Instance(GameServerSettings game, RealmSettings realm, InstanceSettings settings) {
        this.game = game;
        this.realm = realm;
        this.settings = settings;
    }

    // todo on full create a new volatile instance based on the current instance that will shutdown on players = 0

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, game.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        logger.onInstanceStarted(realm, settings);
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
