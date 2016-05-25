package Authentication.Controller;

import Authentication.Model.Account;
import Utilities.Token;

/**
 * @author Robin Duda
 */


interface ClientRequest {
    String realm();
    String account();
    String character();
    String className();
    String sender();
    Token token();
    void write(Object object);
    void unauthorize(); // send http 401 unatuhorized
    void missing(); // send 404
    void conflict(); // send conflict
    void accept(); // send 200
    void error(); // send 500
    Account getAccount();
}
