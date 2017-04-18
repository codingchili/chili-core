package com.codingchili.router;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.*;

import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import com.codingchili.core.listener.transport.*;
import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.codingchili.core.listener.CoreService;

import static io.vertx.core.CompositeFuture.all;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Service implements CoreService {
    private RouterContext context;
    private RouterHandler handler;

    public Service() {}

    public Service(RouterContext context) {
        this.context = context;
    }

    @Override
    public void init(CoreContext core) {
        if (context == null) {
            this.context = new RouterContext(core);
        }
    }

    @Override
    public void stop(Future<Void> stop) {
        context.logger().onServiceStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        List<Future> deployments = new ArrayList<>();

        for (ListenerSettings listener : context.transports()) {
            handler = new RouterHandler(context);

            for (int i = 0; i < context.system().getHandlers(); i++) {
                Future<String> future = Future.future();
                deployments.add(future);
                boolean singleHandlerOnly = false;

                switch (listener.getType()) {
                    case UDP:
                        start(UdpListener::new, listener.getType(), future);
                        singleHandlerOnly = true;
                        break;
                    case TCP:
                        start(TcpListener::new, listener.getType(), future);
                        break;
                    case WEBSOCKET:
                        start(WebsocketListener::new, listener.getType(), future);
                        break;
                    case REST:
                        start(RestListener::new, listener.getType(), future);
                        break;
                }
                if (singleHandlerOnly) {
                    break;
                }
            }
        }
        all(deployments).setHandler(done -> context.logger().onServiceStarted(start));
    }

    private void start(Supplier<CoreListener> listener, WireType type, Future<String> future) {
        context.listener(listener.get()
                        .handler(handler)
                        .settings(() -> context.getListener(type)), future);
    }
}
