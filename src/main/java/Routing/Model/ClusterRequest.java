package Routing.Model;

import Protocols.Authorization.Token;
import Protocols.Request;
import Protocols.Serializer;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.*;

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
        return request.getString(ID_REALM);
    }

    public String instance() {
        return request.getString(ID_INSTANCE);
    }

    @Override
    public void missing() {
        connection.write(new JsonObject().put(ID_ERROR, ERROR_IN_ADDRESS));
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

    @Override
    public String action() {
        return request.getString(ID_ACTION);
    }

    @Override
    public Token token() {
        return Serializer.unpack(request.getJsonObject(ID_TOKEN), Token.class);
    }

    @Override
    public JsonObject data() {
        return request;
    }
}
