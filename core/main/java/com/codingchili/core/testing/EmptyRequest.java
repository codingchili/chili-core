package com.codingchili.core.testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Request;
import com.codingchili.core.security.Token;


/**
 * @author Robin Duda
 *
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
}
