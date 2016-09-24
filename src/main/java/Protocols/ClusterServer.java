package Protocols;

import Configuration.VertxSettings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public abstract class ClusterServer extends AbstractVerticle {
    protected Vertx vertx;

    @Override
    public void start(Future<Void> start) throws Exception {
        Vertx.clusteredVertx(VertxSettings.Configuration(), cluster -> {
            this.vertx = cluster.result();

            if (cluster.succeeded()) {
                initialize(start);
            } else {
                start.fail(cluster.cause());
            }
        });
    }

    public abstract void initialize(Future<Void> start);
}
