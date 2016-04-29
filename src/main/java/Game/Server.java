package Game;

import Utilities.*;
import Game.Model.RealmSettings;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-07.
 * <p>
 * Root game server, starts realm servers.
 */
public class Server implements Verticle {
    private Logger logger;
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        Config.Load();
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, Config.Gameserver.LOGTOKEN);
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        ArrayList<JsonObject> realms = JsonFileReader.readDirectoryObjects("conf/realm/");

        for (JsonObject settings : realms) {
            RealmSettings realm = (RealmSettings) Serializer.unpack(settings.getJsonObject("realm"), RealmSettings.class);
            RemoteAuthentication authentication = (RemoteAuthentication) Serializer.unpack(settings.getJsonObject("authentication"), RemoteAuthentication.class);
            vertx.deployVerticle(new Game.Realm(realm, authentication));
        }

        logger.onServerStarted();
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
