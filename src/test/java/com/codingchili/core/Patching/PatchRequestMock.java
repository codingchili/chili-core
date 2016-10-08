package com.codingchili.core.Patching;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Patching.Controller.PatchRequest;
import com.codingchili.core.Patching.Model.PatchFile;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Shared.*;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
class PatchRequestMock implements PatchRequest {
    private ResponseListener listener;
    private JsonObject data;
    private String action;

    PatchRequestMock(String action, ResponseListener listener, JsonObject data) {
        this.listener = listener;
        this.data = data;
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
        if (data.containsKey(Strings.ID_VERSION)) {
            return data.getString(Strings.ID_VERSION);
        } else {
            return "";
        }
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
