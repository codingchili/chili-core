package com.codingchili.core.protocol;

import io.vertx.core.*;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;

import static com.codingchili.core.configuration.CoreStrings.ERROR_CLUSTERING_REQUIRED;

/**
 * @author Robin Duda
 *
 * Specifies a handler bootstrapper.
 *
 * A service initializes a set of handlers.
 */
public interface CoreService extends Verticle {
    VertxHolder holder = new VertxHolder();

    default String service() {
        return getClass().getName();
    }

    void init(CoreContext context);

    void stop(Future<Void> stop);

    void start(Future<Void> start);

    default Vertx getVertx() {
        return holder.vertx;
    }

    default SystemSettings settings() {
        return Configurations.system();
    }

    default void init(Vertx vertx, Context context) {
        if (!vertx.isClustered()) {
            new ConsoleLogger().log(ERROR_CLUSTERING_REQUIRED, Level.WARNING);
        }
        init(new SystemContext(vertx));
    }

    class VertxHolder {
        Vertx vertx;

        public void initialize(CoreContext core) {
            this.vertx = core.vertx();
        }
    }
}
