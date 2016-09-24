package Logging;


import Configuration.FileConfiguration;
import Configuration.VertxSettings;
import Logging.Configuration.LogProvider;
import Logging.Configuration.LogServerSettings;
import Logging.Controller.LogHandler;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Protocols.ClusterListener;
import Protocols.ClusterServer;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         Receives logging data from the other components and writes it to an elasticsearch cluster or console.
 */
public class Server extends ClusterServer {
    private Logger logger;
    private LogServerSettings settings;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.settings = FileConfiguration.instance().getLogSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void initialize(Future<Void> start) {
        LogProvider provider = new LogProvider(settings, logger, vertx);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new ClusterListener(new LogHandler(provider)));
        }

        logger.onServerStarted(start);
    }


    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped(stop);
    }
}
