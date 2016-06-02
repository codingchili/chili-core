package Meta.Transport;

import Meta.ClientRequest;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Robin Duda
 */
public class ClientRestRequest implements ClientRequest {
    private RoutingContext context;

    public ClientRestRequest(RoutingContext context) {
        this.context = context;
    }

    @Override
    public void error() {

    }

    @Override
    public void unauthorized() {

    }

    @Override
    public void write(Object object) {

    }

    @Override
    public void accept() {

    }

    @Override
    public void missing() {

    }

    @Override
    public void conflict() {

    }

    @Override
    public void file(Buffer buffer) {

    }

    @Override
    public String file() {
        return null;
    }

    @Override
    public String version() {
        return "";
    }
}
