package Logging;


import Utilities.*;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-04-07.
 */
public class Server implements Verticle {
    private TokenFactory tokenFactory;
    private Vertx vertx;
    private Logger logger;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        Config.Load();
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, Config.Logging.LOGTOKEN);
        this.tokenFactory = new TokenFactory(Config.Logging.SECRET);
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        createIndex();

        vertx.createHttpServer().websocketHandler(connection -> {

            connection.handler(data -> {
                Token token = (Token) Serializer.unpack(data.toJsonObject().getJsonObject("token"), Token.class);

                if (tokenFactory.verifyToken(token)) {
                    JsonObject logdata = data.toJsonObject();
                    logdata.remove("token");

                    vertx.createHttpClient().post(
                            Config.Logging.ES_PORT,
                            Config.Logging.ES_REMOTE,
                            Config.Logging.ES_INDEX + "/all/", response -> {

                                response.handler(event -> {
                                    System.out.println(event.toString());
                                });

                            }).end(logdata.encode());

                    System.out.println(data.toString());
                }

            });
        }).listen(Config.Logging.PORT);

        logger.onServerStarted();
        start.complete();
    }

    private void createIndex() {
        vertx.createHttpClient().put(Config.Logging.ES_PORT, Config.Logging.ES_REMOTE, Config.Logging.ES_INDEX, connection -> {
            connection.handler(data -> {
                System.out.println(data.toString());
            });
        })
                .end(new JsonObject()
                        .put("mappings", new JsonObject()
                                .put(Config.Logging.ES_INDEX, new JsonObject()
                                        .put("properties",
                                                new JsonObject().put("time",
                                                        new JsonObject().put("type", "date"))))).encode());
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
