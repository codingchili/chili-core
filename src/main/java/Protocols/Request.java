package Protocols;

import Protocols.Authorization.Token;
import io.vertx.core.buffer.Buffer;
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

    String action();

    Token token();

    JsonObject data();

    int timeout();
}
