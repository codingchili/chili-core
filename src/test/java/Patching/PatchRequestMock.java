package Patching;

import Patching.Controller.PatchRequest;
import Patching.Model.PatchFile;
import Protocols.Util.Token;
import Protocols.Util.Serializer;
import Shared.*;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
class PatchRequestMock implements PatchRequest {
    private ResponseListener listener;
    private JsonObject data;
    private String action;

    PatchRequestMock(ResponseListener listener, JsonObject json, String action) {
        this.listener = listener;
        this.data = json;
        this.action = action;
    }


    @Override
    public void file(PatchFile file) {
        listener.handle(Serializer.json(file), ResponseStatus.ACCEPTED);
    }

    @Override
    public String file() {
        return data.getString("file");
    }

    @Override
    public String version() {
        return data.getString("version");
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
        return null;
    }

    @Override
    public Token token() {
        return null;
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