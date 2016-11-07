package com.codingchili.core.Protocol;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.Security.Token;


/**
 * @author Robin Duda
 */
public class MockRequest implements Request {
    @Override
    public void error(Throwable e) {

    }

    @Override
    public void unauthorized(Throwable e) {

    }

    @Override
    public void write(Object object) {

    }

    @Override
    public void accept() {

    }

    @Override
    public void missing(Throwable e) {

    }

    @Override
    public void conflict(Throwable e) {

    }

    @Override
    public void bad(Throwable e) {

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
