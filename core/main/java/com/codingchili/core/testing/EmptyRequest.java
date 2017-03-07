package com.codingchili.core.testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Request;
import com.codingchili.core.security.Token;


/**
 * @author Robin Duda
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
        return null;
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
        return null;
    }

    @Override
    public int timeout() {
        return 0;
    }
}
