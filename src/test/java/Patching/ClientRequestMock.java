package Patching;

import Patching.Controller.ClientRequest;
import Patching.Model.PatchFile;
import Protocols.Authorization.Token;
import Protocols.Serializer;
import Shared.*;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
class ClientRequestMock implements ClientRequest {
    private ResponseListener listener;
    private JsonObject json;
    private String action;

    ClientRequestMock(ResponseListener listener, JsonObject json, String action) {
        this.listener = listener;
        this.json = json;
        this.action = action;
    }


    @Override
    public void file(PatchFile file) {
        listener.handle(Serializer.json(file), ResponseStatus.ACCEPTED);
    }

    @Override
    public String file() {
        return json.getString("file");
    }

    @Override
    public String version() {
        return json.getString("version");
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
    public Token token() {
        return null;
    }
}
