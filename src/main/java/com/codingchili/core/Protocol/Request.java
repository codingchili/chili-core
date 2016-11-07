package com.codingchili.core.Protocol;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.Security.Token;

/**
 * @author Robin Duda
 */
public interface Request {

    void write(Object object);

    void accept();

    void error(Throwable e);

    void unauthorized(Throwable e);

    void missing(Throwable e);

    void conflict(Throwable e);

    void bad(Throwable e);

    String action();

    String target();

    Token token();

    JsonObject data();

    int timeout();
}
