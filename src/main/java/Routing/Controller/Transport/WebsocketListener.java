package Routing.Controller.Transport;

import Configuration.Strings;
import Logging.Model.Logger;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Protocols.Packet;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Protocols.Serializer;
import Routing.Configuration.RouteProvider;
import Routing.Model.ClusterRequest;
import Routing.Model.WireListener;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

import static Protocols.AuthorizationHandler.Access.PUBLIC;

/**
 * @author Robin Duda
 */
public class WebsocketListener implements Verticle {
    private Protocol<PacketHandler<ClusterRequest>> protocol;
    private RouteProvider provider;
    private WireListener listener;
    private Logger logger;
    private Vertx vertx;

    public WebsocketListener(RouteProvider provider, WireListener listener) {
        this.provider = provider;
        this.protocol = provider.getProtocol();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        vertx.createHttpServer().websocketHandler(socket -> {

            socket.handler(body -> {
                Packet packet = Serializer.unpack(body.toString(), Packet.class);

                ClusterRequest request = new ClusterRequest(data -> {
                    socket.write(Buffer.buffer(data.encode()));
                }, body.toJsonObject(), listener.getTimeout());

                try {
                    protocol.get(packet.getAction(), PUBLIC).handle(request);
                } catch (AuthorizationRequiredException | HandlerMissingException e) {
                    logger.onHandlerMissing(packet.getAction());
                }
            });

        }).listen(listener.getPort());

        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
