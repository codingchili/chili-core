package Game;

import Game.Model.InstanceSettings;
import Utilities.Config;
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
    private Logger logger;
    private Vertx vertx;
    private InstanceSettings instance;
    private String realm;

    public Instance(InstanceSettings instance, String realm) {
        this.instance = instance;
        this.realm = realm;
    }

    // todo on full create a new volatile instance based on the current instance that will shutdown on players = 0

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, Config.Gameserver.LOGTOKEN);
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        logger.onInstanceStarted(instance);
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
