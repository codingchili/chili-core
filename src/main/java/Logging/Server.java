package Logging;


import Configuration.LogServerSettings;
import Utilities.*;
import Configuration.Config;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         Receives logging data from the other components and writes it to an elasticsearch cluster or console.
 */
public class Server implements Verticle {
    private LogServerSettings settings;
    private TokenFactory tokenFactory;
    private Vertx vertx;
    private Logger logger;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.settings = Config.instance().getLogSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
        this.tokenFactory = new TokenFactory(settings.getSecret());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        createIndex();

        vertx.createHttpServer(new HttpServerOptions().setCompressionSupported(true)).websocketHandler(connection -> {

            connection.handler(data -> {
                Token token = (Token) Serializer.unpack(data.toJsonObject().getJsonObject("token"), Token.class);

                if (tokenFactory.verifyToken(token)) {
                    JsonObject logdata = data.toJsonObject();
                    logdata.remove("token");

                    if (settings.getElastic().getEnabled()) {
                        vertx.createHttpClient().post(
                                settings.getElastic().getPort(),
                                settings.getElastic().getRemote(),
                                settings.getElastic().getIndex() + "/all/", response -> {

                                    response.handler(event -> {
                                    });

                                }).end(logdata.encode());
                    }

                    if (settings.getConsole())
                        System.out.println(logdata);
                }

            });
        }).listen(settings.getPort());

        logger.onServerStarted();
        start.complete();
    }

    private void createIndex() {
        vertx.createHttpClient().put(
                settings.getElastic().getPort(),
                settings.getElastic().getRemote(),
                settings.getElastic().getIndex(), connection -> {

                    connection.handler(data -> {
                        System.out.println(data.toString());
                    });
                })
                .end(settings.getElastic().getTemplate().toString());
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped();
        stop.complete();
    }
}
