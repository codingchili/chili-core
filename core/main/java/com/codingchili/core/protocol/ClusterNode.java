package com.codingchili.core.protocol;

import io.vertx.core.*;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

import static com.codingchili.core.configuration.CoreStrings.ERROR_CLUSTERING_REQUIRED;

/**
 * @author Robin Duda
 *
 * A node in the cluster, all startable services should
 * implement this class for the Launcher to accept it.
 */
public abstract class ClusterNode implements Verticle {
    protected Vertx vertx;
    protected SystemSettings settings;

    @Override
    public void init(Vertx vertx, Context context) {
        this.settings = Configurations.system();
        this.vertx = vertx;

        if (!vertx.isClustered()) {
            new ConsoleLogger().log(ERROR_CLUSTERING_REQUIRED, Level.WARNING);
        }
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        start.complete();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}
