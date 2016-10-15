package com.codingchili.core.Website;

import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Protocols.ResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *
 * Used to mock a request carrying a buffer only.
 */
class WebRequestMock implements Request {
    private ResponseListener listener;
    private JsonObject data;
    private String action;
    private Buffer buffer;

    WebRequestMock(String action, ResponseListener listener, JsonObject data) {
        this.listener = listener;
        this.data = data;
        this.action = action;
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
        this.buffer = (Buffer) object;
        listener.handle(Serializer.json(object), ResponseStatus.ACCEPTED);
    }

    Buffer getBuffer() {
        return buffer;
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
