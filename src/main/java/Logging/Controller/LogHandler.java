package Logging.Controller;

import Configuration.Logserver.LogServerSettings;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Protocols.Serializer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class LogHandler extends AbstractVerticle {
    private LogServerSettings settings;
    private TokenFactory tokenFactory;

    public LogHandler(Vertx vertx, LogServerSettings settings) {
        this.vertx = vertx;
        this.settings = settings;
        this.tokenFactory = new TokenFactory(settings.getSecret());
    }

    public void start(Future<Void> start) {
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

}
