package Authentication.Controller;

import Authentication.Model.Account;
import Protocols.Authentication.ClientAuthentication;
import Protocols.Request;
import Protocols.Authorization.Token;

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

    void authenticate(ClientAuthentication authentication);
}
