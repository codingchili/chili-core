package Routing.Controller.Transport;

import Configuration.Routing;
import Logging.Model.DefaultLogger;
import Protocols.Util.Token;
import Protocols.ClusterVerticle;
import Protocols.Request;
import Routing.Configuration.RoutingSettings;
import Routing.Controller.RoutingHandler;
import Routing.Model.ListenerSettings;
import Routing.Model.WireType;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 */
public class RestListener extends ClusterVerticle {
    private RoutingHandler handler;
    private ListenerSettings listener;
    private RoutingSettings settings;
    private HttpServerOptions options;
    private Router router;


    public RestListener(RoutingHandler handler, RoutingSettings settings) {
        this.handler = handler;
        this.settings = settings;
        this.listener = settings.getListener(WireType.REST);

        options = new HttpServerOptions().setCompressionSupported(false);
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        logger = new DefaultLogger(vertx, settings.getLogserver());
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        Routing.EnableCors(router);
        router.route("/*").handler(this::packet);
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer(options).requestHandler(router::accept).listen(listener.getPort());
    }

    private void packet(RoutingContext context) {
        HttpServerRequest request = context.request();

        handler.handle(new Request() {
            @Override
            public void error() {
                send(request, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public void unauthorized() {
                send(request, HttpResponseStatus.UNAUTHORIZED);
            }

            @Override
            public void write(Object object) {
                if (object instanceof Buffer) {
                    send(request, (Buffer) object);
                } else {
                    send(request, object);
                }
            }

            @Override
            public void accept() {
                send(request, HttpResponseStatus.OK);
            }

            @Override
            public void missing() {
                send(request, HttpResponseStatus.NOT_FOUND);
            }

            @Override
            public void conflict() {
                send(request, HttpResponseStatus.CONFLICT);
            }

            @Override
            public String action() {
                String action = request.path();

                try {
                    JsonObject data = context.getBodyAsJson();

                    if (data != null && data.containsKey(ID_ACTION)) {
                        action = data.getString(ID_ACTION);
                    }
                } catch (DecodeException ignored) {
                }

                return action;
            }

            @Override
            public String target() {
                String target = NODE_WEBSERVER;

                try {
                    JsonObject data = context.getBodyAsJson();

                    if (data != null && data.containsKey(ID_TARGET)) {
                        target = data.getString(ID_TARGET);
                    }
                } catch (DecodeException ignored) {
                }

                return target;
            }

            @Override
            public Token token() {
                return null;
            }

            @Override
            public JsonObject data() {
                try {
                    return context.getBodyAsJson().put(ID_ACTION, action());
                } catch (DecodeException e) {
                    return new JsonObject().put(ID_ACTION, request.path());
                }
            }

            @Override
            public int timeout() {
                return listener.getTimeout();
            }
        });
    }

    private void send(HttpServerRequest request, HttpResponseStatus status) {
        request.response().setStatusCode(status.code()).end();
    }

    private void send(HttpServerRequest request, Buffer buffer) {
        request.response().end(buffer);
    }

    private void send(HttpServerRequest request, Object object) {
        request.response().end(Buffer.buffer(object.toString()));
    }
}
