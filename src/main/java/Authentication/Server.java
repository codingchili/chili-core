package Authentication;

import Authentication.Controller.ClientHandler;
import Authentication.Controller.RealmHandler;
import Authentication.Model.AccountDB;
import Authentication.Model.AsyncAccountStore;
import Configuration.AuthServerSettings;
import Configuration.Config;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by Robin on 2016-04-07.
 */
public class Server implements Verticle {
    private Vertx vertx;
    private Logger logger;
    private AuthServerSettings settings;

    public Server() {
        this.settings = Config.instance().getAuthSettings();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
        AsyncAccountStore accounts = new AccountDB(
                MongoClient.createShared(vertx, new JsonObject()
                        .put("db_name", settings.getDatabase().getName())
                        .put("connection_string", settings.getDatabase().getRemote())));

        new ClientHandler(vertx, logger, accounts, new RealmHandler(vertx, logger, accounts));
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        logger.onServerStarted();
        start.complete();
    }


    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
