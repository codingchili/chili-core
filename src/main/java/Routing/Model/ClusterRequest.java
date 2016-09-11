package Routing.Model;

import Configuration.Strings;
import Protocols.Request;
import Protocols.Serializer;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class ClusterRequest implements Request {
    private JsonObject request;
    private WireConnection connection;
    private long timeout;

    public ClusterRequest(WireConnection connection, JsonObject request, long timeout) {
        this.request = request;
        this.connection = connection;
        this.timeout = timeout;
    }

    @Override
    public void write(Object object) {
        connection.write(Serializer.json(object));
    }

    public JsonObject getMessage() {
        return request;
    }

    public String realm() {
        return request.getString(Strings.ID_REALM);
    }

    public String instance() {
        return request.getString(Strings.ID_INSTANCE);
    }

    @Override
    public void missing() {
        connection.write(new JsonObject().put(Strings.ID_ERROR, Strings.ERROR_IN_ADDRESS));
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public void error() {
    }

    @Override
    public void unauthorized() {
    }

    @Override
    public void accept() {
    }

    @Override
    public void conflict() {
    }
}
