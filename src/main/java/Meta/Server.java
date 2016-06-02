package Meta;

import Meta.Transport.ClientServer;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         website and resource server.
 */
public class Server implements Verticle {
    private Vertx vertx;
    private MetaProvider provider;

    public Server(MetaProvider provider) {
        this.provider = provider;
    }

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
        new ClientHandler(provider);

        vertx.deployVerticle(new ClientServer(provider));

        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
