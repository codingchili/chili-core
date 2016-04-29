package Utilities;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Created by Robin on 2016-04-28.
 */
public class TokenSigner implements Verticle {
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        Config.Load();
        this.vertx = vertx;

        if (context.processArgs().size() >= 4) {
            switch (context.processArgs().get(2)) {
                case "logger":
                    signToken(Config.Logging.SECRET, context.processArgs().get(3), "logger");
                    break;
                case "server":
                    signToken(Config.Authentication.REALM_SECRET, context.processArgs().get(3), "server");
                    break;
                default:
                    throw new RuntimeException("Error: Token type not found.");
            }
        }
    }

    private void signToken(byte[] secret, String domain, String type) {
        System.out.println("generated key of type " + type + "\r\n" + Serializer.json(new Token(new TokenFactory(secret), domain)).encodePrettily());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
