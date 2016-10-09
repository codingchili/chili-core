package com.codingchili.core.Protocols;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public abstract class ClusterVerticle implements Verticle {
    protected Vertx vertx;
    protected Logger logger;

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        if (!vertx.isClustered()) {
            throw new RuntimeException(Strings.ERROR_CLUSTERING_REQUIRED);
        }
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        System.out.println("SERVER STOPPED");
        System.out.flush();
        logger.onServerStopped(stop);
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}
