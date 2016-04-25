package Logging;

import Configuration.Config;
import Configuration.Config.Address;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-04-07.
 */
public class Server implements Verticle {
    private Vertx vertx;
    private Logger logger;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        Config.Load();
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        vertx.createHttpServer().websocketHandler(websocket -> {

            websocket.handler(data -> {
                System.out.println(data.toString());
            });

            websocket.endHandler(end -> {
                System.out.println();
            });

        }).listen(Config.Logging.PORT);

        logger = new DefaultLogger(vertx, "Logserver");
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
