package Logging.Controller;

import Configuration.Strings;
import Logging.Configuration.LogServerSettings;
import Logging.Model.ConsoleLogger;
import Logging.Model.ElasticLogger;
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
    private ConsoleLogger console;
    private ElasticLogger elastic;

    public LogHandler(Vertx vertx, LogServerSettings settings) {
        this.vertx = vertx;
        this.settings = settings;
        this.tokenFactory = new TokenFactory(settings.getSecret());
        this.console = new ConsoleLogger(settings.getConsole());
        this.elastic = new ElasticLogger(settings.getElastic(), vertx);
    }

    public void start(Future<Void> start) {
        vertx.createHttpServer(new HttpServerOptions().setCompressionSupported(true)).websocketHandler(connection -> {

            connection.handler(data -> {
                JsonObject message = data.toJsonObject();

                if (message.containsKey(Strings.ID_TOKEN)) {
                    Token token = Serializer.unpack(data.toJsonObject().getJsonObject(Strings.ID_TOKEN), Token.class);

                    if (tokenFactory.verifyToken(token)) {
                        JsonObject logdata = data.toJsonObject();
                        logdata.remove(Strings.ID_TOKEN);

                        elastic.log(logdata);
                        console.log(logdata);
                    }
                }
            });
        }).listen(settings.getPort());
    }
}
