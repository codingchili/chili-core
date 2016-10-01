package Logging;

import Protocols.Util.Token;
import Protocols.Request;
import Protocols.Util.Serializer;
import Shared.ResponseListener;
import Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.ID_TOKEN;

/**
 * @author Robin Duda
 */
class LogRequestMock implements Request {
    private ResponseListener listener;
    private String action;
    private JsonObject data;

    LogRequestMock(String action, ResponseListener listener, JsonObject data) {
        this.action = action;
        this.listener = listener;
        this.data = (data == null) ? new JsonObject() : data;
    }

    @Override
    public void error() {
        listener.handle(null, ResponseStatus.ERROR);
    }

    @Override
    public void unauthorized() {
        listener.handle(null, ResponseStatus.UNAUTHORIZED);
    }

    @Override
    public void write(Object object) {
        listener.handle(Serializer.json(object), ResponseStatus.ACCEPTED);
    }

    @Override
    public void accept() {
        listener.handle(null, ResponseStatus.ACCEPTED);
    }

    @Override
    public void missing() {
        listener.handle(null, ResponseStatus.MISSING);
    }

    @Override
    public void conflict() {
        listener.handle(null, ResponseStatus.CONFLICT);
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public String target() {
        return "";
    }

    @Override
    public Token token() {
        return Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class);
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return 0;
    }
}
