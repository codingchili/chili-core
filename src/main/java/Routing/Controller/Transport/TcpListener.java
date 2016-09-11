package Routing.Controller.Transport;

import Routing.Configuration.RouteProvider;
import Routing.Model.WireListener;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class TcpListener implements Verticle {
    public TcpListener(RouteProvider provider, WireListener listener) {

    }

    @Override
    public Vertx getVertx() {
        return null;
    }

    @Override
    public void init(Vertx vertx, Context context) {

    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {

    }
}
