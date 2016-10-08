package com.codingchili.core.Website;

import com.codingchili.core.Protocols.ClusterListener;
import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Website.Configuration.WebserverProvider;
import com.codingchili.core.Website.Controller.WebHandler;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class Server extends ClusterVerticle {
    private WebserverProvider provider;

    public Server() {
    }

    public Server(WebserverProvider provider) {
        this.provider = provider;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        if (provider == null) {
            this.provider = new WebserverProvider(vertx);
        }

        this.logger = provider.getLogger();
    }

    @Override
    public void start(Future<Void> start) throws Exception {

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            vertx.deployVerticle(new ClusterListener(new WebHandler(provider)));
        }

        logger.onServerStarted(start);
    }
}
