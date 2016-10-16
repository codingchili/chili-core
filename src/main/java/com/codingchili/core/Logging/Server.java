package com.codingchili.core.Logging;


import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Logging.Configuration.LogProvider;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Logging.Controller.LogHandler;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Protocols.ClusterListener;
import com.codingchili.core.Protocols.ClusterVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import static com.codingchili.core.Configuration.Strings.PATH_LOGSERVER;

/**
 * @author Robin Duda
 *         Receives logging data from the other components and writes it to an elasticsearch cluster or console.
 */
public class Server extends ClusterVerticle {
    private LogServerSettings settings;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.settings = FileConfiguration.get(PATH_LOGSERVER, LogServerSettings.class);
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) {
        LogProvider provider = new LogProvider(settings, logger, vertx);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new ClusterListener(new LogHandler(provider)));
        }

        logger.onServerStarted(start);
    }
}
