package com.codingchili.website;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.website.configuration.WebserverContext;
import com.codingchili.website.controller.WebHandler;
import io.vertx.core.Future;

import static com.codingchili.core.context.FutureHelper.generic;

/**
 * @author Robin Duda
 * <p>
 * Service for the webserver.
 */
public class Service implements CoreService {
    private WebserverContext context;

    public Service() {
    }

    public Service(WebserverContext context) {
        this.context = context;
    }

    @Override
    public void init(CoreContext core) {
        if (this.context == null) {
            this.context = new WebserverContext(core);
        }
    }

    @Override
    public void start(Future<Void> start) {
        context.handler(() -> new WebHandler(context)).setHandler(generic(start));
    }
}
