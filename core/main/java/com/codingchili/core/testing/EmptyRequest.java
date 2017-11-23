package com.codingchili.core.testing;

import com.codingchili.core.listener.Request;
import com.codingchili.core.security.Token;
import io.vertx.core.json.JsonObject;


/**
 * @author Robin Duda
 * <p>
 * An empty request for testing.
 */
public class EmptyRequest implements Request {
    @Override
    public void error(Throwable exception) {

    }

    @Override
    public void write(Object object) {

    }

    @Override
    public void accept() {

    }

    @Override
    public String route() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String target() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Token token() {
        throw new UnsupportedOperationException();

    }

    @Override
    public JsonObject data() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int timeout() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int maxSize() {
        return 0;
    }
}
