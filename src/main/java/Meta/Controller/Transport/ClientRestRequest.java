package Meta.Controller.Transport;

import Meta.Controller.ClientRequest;
import Protocols.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Robin Duda
 */
class ClientRestRequest implements ClientRequest {
    private RoutingContext context;

    ClientRestRequest(RoutingContext context) {
        this.context = context;
    }

    @Override
    public void error() {
        sendStatus(500);
    }

    private void sendStatus(int code) {
        context.response().setStatusCode(code).end();
    }

    @Override
    public void unauthorized() {
        sendStatus(401);
    }

    @Override
    public void write(Object object) {
        context.response().end(Serializer.pack(object));
    }

    @Override
    public void accept() {
        context.response().end();
    }

    @Override
    public void missing() {
        sendStatus(404);
    }

    @Override
    public void conflict() {
        sendStatus(409);
    }

    @Override
    public void file(Buffer buffer) {
        context.response().putHeader("content-type", "image/svg+xml").end(buffer);
    }

    @Override
    public String file() {
        return context.request().getParam("file");
    }

    @Override
    public String version() {
        String version = context.request().getParam("version");

        if (version == null) {
            version = "999999999999999999999999999999999999";
        }

        return version;
    }
}
