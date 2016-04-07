package Logging;

import Configuration.Configuration.Address;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Created by Robin on 2016-04-07.
 */
public class LogServer implements Verticle {
    private Vertx vertx;
    private Logger logger;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        vertx.eventBus().consumer(Address.LOGS, result -> {
            System.out.println(result.body());
        });

        logger = new DefaultLogger(vertx, this.getClass().getSimpleName());
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
