package Authentication.Controller;

import Authentication.Model.Account;
import Protocols.Request;
import Protocols.Util.Token;

/**
 * @author Robin Duda
 */


public interface ClientRequest extends Request {
    String realmName();

    String account();

    String character();

    String className();

    String sender();

    Token token();

    Account getAccount();
}
