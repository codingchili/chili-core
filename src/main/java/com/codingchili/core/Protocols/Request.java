package com.codingchili.core.Protocols;

import com.codingchili.core.Protocols.Util.Token;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public interface Request {

    void error();

    void unauthorized();

    void write(Object object);

    void accept();

    void missing();

    void conflict();

    void bad();

    String action();

    String target();

    Token token();

    JsonObject data();

    int timeout();
}
