package com.codingchili.core.Protocols;

import com.codingchili.core.Protocols.Util.Token;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class MockRequest implements Request {
    @Override
    public void error() {

    }

    @Override
    public void unauthorized() {

    }

    @Override
    public void write(Object object) {

    }

    @Override
    public void accept() {

    }

    @Override
    public void missing() {

    }

    @Override
    public void conflict() {

    }

    @Override
    public String action() {
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
