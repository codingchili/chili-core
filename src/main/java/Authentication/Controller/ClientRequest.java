package Authentication.Controller;

import Authentication.Model.Account;
import Protocol.Authentication.ClientAuthentication;
import Protocol.Authentication.RealmMetaData;
import Utilities.Token;

import java.util.ArrayList;

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
