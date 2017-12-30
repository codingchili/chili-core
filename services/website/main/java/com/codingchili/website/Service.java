package com.codingchili.website;

import com.codingchili.website.configuration.WebserverContext;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.listener.ListenerSettings;

import static com.codingchili.core.context.FutureHelper.untyped;

/**
 * @author Robin Duda
 * <p>
 * Service for the webserver.
 */
public class Service implements CoreService {
    private static final String POLYMER = "website/";
    private WebserverContext core;

    @Override
    public void init(CoreContext core) {
        this.core = new WebserverContext(core);
    }

    @Override
    public void start(Future<Void> start) {
        Router router = Router.router(core.vertx());
        router.route().handler(BodyHandler.create());

        router.route("/*").handler(StaticHandler.create()
                .setCachingEnabled(false)
                .setWebRoot(POLYMER));

        core.vertx().createHttpServer(new ListenerSettings().getHttpOptions(core))
                .requestHandler(router::accept)
                .listen(443, untyped(start));
    }
}
