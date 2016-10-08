package com.codingchili.core.Routing;

import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Routing.Controller.Transport.RestRouteRequest;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class RouteRequestMock extends RestRouteRequest {
    private ResponseListener listener;
    private String target;
    private JsonObject data;

    RouteRequestMock(String target, ResponseListener listener, JsonObject data) {
        super(null, null, null);

        this.listener = listener;
        this.target = target;
        this.data = data;
    }

    @Override
    public void write(Object object) {
        listener.handle(Serializer.json(object), ResponseStatus.ACCEPTED);
    }

    @Override
    public void missing() {
        listener.handle(null, ResponseStatus.MISSING);
    }

    @Override
    public void error() {
        listener.handle(null, ResponseStatus.ERROR);
    }

    @Override
    public String target() {
        return target;
    }

    @Override
    public String action() {
        return target;
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return 5000;
    }
}
