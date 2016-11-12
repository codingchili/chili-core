package com.codingchili.core.Testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.Protocol.Request;
import com.codingchili.core.Security.Token;


/**
 * @author Robin Duda
 */
public class EmptyRequest implements Request {
    @Override
    public void error(Throwable exception) {

    }

    @Override
    public void unauthorized(Throwable exception) {

    }

    @Override
    public void write(Object object) {

    }

    @Override
    public void accept() {

    }

    @Override
    public void missing(Throwable exception) {

    }

    @Override
    public void conflict(Throwable exception) {

    }

    @Override
    public void bad(Throwable exception) {

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
