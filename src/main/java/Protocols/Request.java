package Protocols;

import Protocols.Util.Token;
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

    String target();

    Token token();

    JsonObject data();

    int timeout();
}
